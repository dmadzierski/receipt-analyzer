package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import pl.madzierski.daniel.file_group.model.FileDto;

@AllArgsConstructor
public class FileFactory {
    FileDto toDto(File file) {
        return new FileDto(file.getId(), file.getPath(), file.getRawData(), file.getPartNumber());
    }
}
