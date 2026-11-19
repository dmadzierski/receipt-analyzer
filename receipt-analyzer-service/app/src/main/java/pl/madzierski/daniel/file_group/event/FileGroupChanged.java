package pl.madzierski.daniel.file_group.event;

import pl.madzierski.daniel.event.AggregateChanged;
import pl.madzierski.daniel.file_group.FileType;

import java.util.List;
import java.util.UUID;

public record FileGroupChanged(UUID id, long version, UUID receiptId, UUID ownerId, FileType fileType, List<File> files) implements AggregateChanged {

    public record File(UUID id, String path, Integer partNumber) {
    }
}
