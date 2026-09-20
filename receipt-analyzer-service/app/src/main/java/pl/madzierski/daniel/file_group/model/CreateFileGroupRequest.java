package pl.madzierski.daniel.file_group.model;

import pl.madzierski.daniel.file_group.FileType;

public record CreateFileGroupRequest(FileType fileType, boolean isOriginal) {
}
