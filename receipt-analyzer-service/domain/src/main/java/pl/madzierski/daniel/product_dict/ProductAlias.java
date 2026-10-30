package pl.madzierski.daniel.product_dict;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(exclude = "productDict")
class ProductAlias {

    private UUID id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String name;
    private ProductDict productDict;

    public ProductAlias(UUID id, String name, ProductDict productDict) {
        this.id = id;
        this.name = name;
        this.productDict = productDict;
    }
}