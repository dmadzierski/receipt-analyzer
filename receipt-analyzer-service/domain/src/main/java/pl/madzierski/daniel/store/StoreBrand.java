package pl.madzierski.daniel.store;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pl.madzierski.daniel.exception.AppIllegalStateException;
import pl.madzierski.daniel.exception.AppIllegalStateExceptionMessages;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
class StoreBrand {

    private UUID id;
    private Long version;
    private String name;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    static StoreBrand create(String name) {
        if (name == null || name.isBlank()) throw new AppIllegalStateException(AppIllegalStateExceptionMessages.BLANK_NAME);
        return builder()
            .name(name.trim())
            .build();
    }

    static StoreBrand restore(UUID id, Long version, String name, LocalDateTime createdDate, LocalDateTime modifiedDate) {
        return builder()
            .id(id)
            .version(version)
            .name(name)
            .createdDate(createdDate)
            .modifiedDate(modifiedDate)
            .build();
    }
}
