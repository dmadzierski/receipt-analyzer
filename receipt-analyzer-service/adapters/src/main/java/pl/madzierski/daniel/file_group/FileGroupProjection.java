package pl.madzierski.daniel.file_group;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.madzierski.daniel.event.IntegrationEvent;
import pl.madzierski.daniel.file_group.event.FileGroupChanged;
import pl.madzierski.daniel.file_group.event.FileGroupDeleted;
import pl.madzierski.daniel.readmodel.Projection;
import pl.madzierski.daniel.receipt.event.ReceiptDeleted;

@Component
@RequiredArgsConstructor
class FileGroupProjection implements Projection {

    private final MongoFileGroupRepository repository;

    @Override
    public void apply(IntegrationEvent event) {
        switch (event) {
            case FileGroupChanged changed -> repository.save(MongoFileGroup.fromEvent(changed));
            case FileGroupDeleted deleted -> repository.deleteById(deleted.id());
            case ReceiptDeleted deleted -> repository.deleteAllByReceiptId(deleted.id());
            default -> {
            }
        }
    }

    @Override
    public void clear() {
        repository.deleteAll();
    }
}
