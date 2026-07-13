package pl.madzierski.daniel.file_group.model;

import java.util.Collections;
import java.util.Set;

public record FileGroupDto(Set<FileDto> files) {

    @Override
    public Set<FileDto> files() {
        return Collections.unmodifiableSet(files);
    }
}
