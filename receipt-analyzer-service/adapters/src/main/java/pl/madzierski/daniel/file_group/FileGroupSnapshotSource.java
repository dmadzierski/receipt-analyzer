package pl.madzierski.daniel.file_group;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.madzierski.daniel.event.IntegrationEvent;
import pl.madzierski.daniel.readmodel.ReadModelSnapshotSource;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
class FileGroupSnapshotSource implements ReadModelSnapshotSource {

    private final SqlFileGroupRepository repository;

    @Override
    public int order() {
        return 50;
    }

    @Override
    public Stream<IntegrationEvent> snapshots() {
        return repository.findAll().stream().map(SqlFileGroup::toFileGroup).map(FileGroupEventMapper::changed);
    }
}
