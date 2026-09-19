package pl.madzierski.daniel.file_group.model;

import lombok.Builder;
import pl.madzierski.daniel.file_group.FileType;

import java.util.Collections;
import java.util.Set;

@Builder
public record FileGroupDto(String id, FileType fileType, boolean isOriginal, Set<FileDto> files) {

    @Override
    public Set<FileDto> files() {
        return Collections.unmodifiableSet(files);
    }
}
