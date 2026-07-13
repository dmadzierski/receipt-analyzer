package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
enum FileType {
    PDF("application/pdf", "pdf"),
    JSON("application/json", "json");

    private final String mimeType;
    private final String extension;

    public static FileType invoke(String contentType) {
        for (FileType fileType : FileType.values()) {
            if (fileType.mimeType.equals(contentType)) {
                return fileType;
            }
        }
        throw new IllegalArgumentException("Unsupported content type: " + contentType);
    }

}