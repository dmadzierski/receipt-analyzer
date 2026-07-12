package pl.madzierski.daniel.app.file_group;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FileType {
    PDF("application/pdf", "pdf"),
    JSON("application/json", "json");

    private final String name;
    private final String extension;

    public static FileType invoke(String contentType) {
        for (FileType fileType : FileType.values()) {
            if (fileType.name.equals(contentType)) {
                return fileType;
            }
        }
        throw new IllegalArgumentException("Unsupported content type: " + contentType);
    }

}