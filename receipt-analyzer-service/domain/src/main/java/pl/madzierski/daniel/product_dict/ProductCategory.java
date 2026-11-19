package pl.madzierski.daniel.product_dict;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
class ProductCategory {

    private UUID id;
    private Long version;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String name;

    public ProductCategory(String name) {
        this.name = name;
    }

}