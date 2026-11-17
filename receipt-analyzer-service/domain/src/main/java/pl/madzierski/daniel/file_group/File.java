package pl.madzierski.daniel.file_group;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
class File {

    private UUID id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String path;
    private Integer partNumber;

    static File create(int partNumber) {
        return builder()
            .partNumber(partNumber)
            .build();
    }

    static File restore(UUID id, LocalDateTime createdDate, LocalDateTime modifiedDate, String path, Integer partNumber) {
        return builder()
            .id(id)
            .createdDate(createdDate)
            .modifiedDate(modifiedDate)
            .path(path)
            .partNumber(partNumber)
            .build();
    }

    void assignPath(String path) {
        this.path = path;
    }
}
