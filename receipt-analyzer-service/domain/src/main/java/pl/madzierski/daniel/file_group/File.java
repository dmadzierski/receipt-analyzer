package pl.madzierski.daniel.file_group;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(exclude = "fileGroup")
class File {

    private UUID id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private FileGroup fileGroup;
    private String path;
    private String rawData;
    private Integer partNumber;

    public File(FileGroup fileGroup, Integer partNumber) {
        this.fileGroup = fileGroup;
        this.partNumber = partNumber;
    }
}