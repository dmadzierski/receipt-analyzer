package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import pl.madzierski.daniel.file_group.model.FileGroupDto;

import java.util.stream.Collectors;

@AllArgsConstructor
public class FileGroupFactory {

    private final FileFactory fileFactory;

    FileGroupDto toDto(FileGroup fileGroup) {
        return new FileGroupDto(fileGroup.getId(),
                fileGroup.getFileType(),
                fileGroup.getIsOriginal(),
                fileGroup.getFiles()
                        .stream()
                        .map(fileFactory::toDto)
                        .collect(Collectors.toSet()));
    }
}
