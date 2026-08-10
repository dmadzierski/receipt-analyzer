package pl.madzierski.daniel.file_group.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FileDto {
    private String id;
    private String path;
    private String rawData;
    private Integer partNumber;

    public FileDto(String id, String path, Integer partNumber) {
        this.id = id;
        this.path = path;
        this.partNumber = partNumber;
    }
}
