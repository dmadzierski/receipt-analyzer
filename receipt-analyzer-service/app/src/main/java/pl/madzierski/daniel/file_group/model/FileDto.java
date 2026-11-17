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
    private Integer partNumber;
}
