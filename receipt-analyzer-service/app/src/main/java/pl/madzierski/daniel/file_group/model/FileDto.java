package pl.madzierski.daniel.file_group.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileDto {
    private String id;
    private String path;
    private String rawData;
    private Integer partNumber;
}
