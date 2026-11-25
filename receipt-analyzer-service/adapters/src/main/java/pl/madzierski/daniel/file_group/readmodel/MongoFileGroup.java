package pl.madzierski.daniel.file_group.readmodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import pl.madzierski.daniel.file_group.FileType;
import pl.madzierski.daniel.file_group.event.FileGroupChanged;
import pl.madzierski.daniel.file_group.model.FileDto;
import pl.madzierski.daniel.file_group.model.FileGroupDto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Document("file_groups")
record MongoFileGroup(@Id UUID id, @Indexed UUID receiptId, UUID ownerId, FileType fileType, List<File> files) {

    record File(UUID id, String path, Integer partNumber) {
    }

    static MongoFileGroup fromEvent(FileGroupChanged event) {
        return new MongoFileGroup(event.id(), event.receiptId(), event.ownerId(), event.fileType(),
            event.files().stream().map(file -> new File(file.id(), file.path(), file.partNumber())).toList());
    }

    FileGroupDto toDto() {
        return new FileGroupDto(id, fileType, files.stream()
            .map(file -> new FileDto(file.id(), file.path(), file.partNumber()))
            .collect(Collectors.toSet()));
    }
}
