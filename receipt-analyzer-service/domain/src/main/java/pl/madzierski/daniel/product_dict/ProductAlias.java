package pl.madzierski.daniel.product_dict;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
class ProductAlias {

    private UUID id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String name;

    static ProductAlias create(String name) {
        return builder()
            .name(name)
            .build();
    }

    static ProductAlias restore(UUID id, LocalDateTime createdDate, LocalDateTime modifiedDate, String name) {
        return builder()
            .id(id)
            .createdDate(createdDate)
            .modifiedDate(modifiedDate)
            .name(name)
            .build();
    }
}
