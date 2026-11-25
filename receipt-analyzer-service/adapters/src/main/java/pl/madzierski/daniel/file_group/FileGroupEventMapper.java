package pl.madzierski.daniel.file_group;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.madzierski.daniel.file_group.event.FileGroupChanged;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class FileGroupEventMapper {

    static FileGroupChanged changed(FileGroup fileGroup) {
        return new FileGroupChanged(fileGroup.getId(), fileGroup.getVersion(), fileGroup.getReceiptId(),
            fileGroup.getOwner().id(), fileGroup.getFileType(), fileGroup.getFiles().stream()
            .map(file -> new FileGroupChanged.File(file.getId(), file.getPath(), file.getPartNumber()))
            .toList());
    }
}
