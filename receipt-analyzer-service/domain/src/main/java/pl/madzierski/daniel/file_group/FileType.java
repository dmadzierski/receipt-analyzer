package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
@Getter
public enum FileType {
    PDF("application/pdf", "pdf"),
    JSON("application/json", "json"),
    JPEG("image/jpeg", "jpg"),
    PNG("image/png", "png"),
    WEBP("image/webp", "webp");

    private final String mimeType;
    private final String extension;

    public static Optional<FileType> invoke(String mimeType) {
        return Arrays.stream(values())
            .filter(fileType -> fileType.mimeType.equals(mimeType))
            .findFirst();
    }

}