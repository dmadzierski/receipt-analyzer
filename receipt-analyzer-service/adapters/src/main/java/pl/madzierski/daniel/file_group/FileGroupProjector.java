package pl.madzierski.daniel.file_group;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.madzierski.daniel.event.IntegrationEvent;
import pl.madzierski.daniel.file_group.event.FileGroupChanged;
import pl.madzierski.daniel.file_group.event.FileGroupDeleted;
import pl.madzierski.daniel.readmodel.Projector;

@Component
@RequiredArgsConstructor
class FileGroupProjector implements Projector {

    private final MongoFileGroupRepository repository;

    @Override
    public void apply(IntegrationEvent event) {
        switch (event) {
            case FileGroupChanged changed -> repository.save(MongoFileGroup.fromEvent(changed));
            case FileGroupDeleted deleted -> repository.deleteById(deleted.id());
            default -> {
            }
        }
    }

    @Override
    public void clear() {
        repository.deleteAll();
    }
}
