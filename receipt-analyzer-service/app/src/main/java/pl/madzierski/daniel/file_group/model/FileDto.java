package pl.madzierski.daniel.file_group.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class FileDto {
    private UUID id;
    private String path;
    private String rawData;
    private Integer partNumber;

    public FileDto(UUID id, String path, Integer partNumber) {
        this.id = id;
        this.path = path;
        this.partNumber = partNumber;
    }
}
