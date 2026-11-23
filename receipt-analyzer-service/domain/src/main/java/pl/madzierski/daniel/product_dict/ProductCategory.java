package pl.madzierski.daniel.product_dict;

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
class ProductCategory {

    private UUID id;
    private Long version;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String name;

    static ProductCategory create(String name) {
        return builder()
            .name(requireName(name))
            .build();
    }

    static ProductCategory restore(UUID id, Long version, LocalDateTime createdDate, LocalDateTime modifiedDate, String name) {
        return builder()
            .id(id)
            .version(version)
            .createdDate(createdDate)
            .modifiedDate(modifiedDate)
            .name(name)
            .build();
    }

    void rename(String name) {
        this.name = requireName(name);
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) throw new AppIllegalStateException(AppIllegalStateExceptionMessages.BLANK_NAME);
        return name.trim();
    }
}
