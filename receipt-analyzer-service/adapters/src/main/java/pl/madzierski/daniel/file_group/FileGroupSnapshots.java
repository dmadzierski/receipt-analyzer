package pl.madzierski.daniel.file_group;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.madzierski.daniel.event.IntegrationEvent;
import pl.madzierski.daniel.file_group.event.FileGroupChanged;
import pl.madzierski.daniel.readmodel.ReadModelSnapshotSource;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
class FileGroupSnapshots implements ReadModelSnapshotSource {

    private final SqlFileGroupRepository repository;

    static FileGroupChanged changed(FileGroup fileGroup) {
        return new FileGroupChanged(fileGroup.getId(), fileGroup.getVersion(), fileGroup.getReceipt() != null ? fileGroup.getReceipt().id() : null,
            fileGroup.getOwner().id(), fileGroup.getFileType(), fileGroup.getFiles().stream()
            .map(file -> new FileGroupChanged.File(file.getId(), file.getPath(), file.getPartNumber()))
            .toList());
    }

    @Override
    public int order() {
        return 50;
    }

    @Override
    public Stream<IntegrationEvent> snapshots() {
        return repository.findAll().stream().map(SqlFileGroup::toFileGroup).map(FileGroupSnapshots::changed);
    }
}
