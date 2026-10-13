package pl.madzierski.daniel.product_dict;

import lombok.*;

import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(exclude = "productDict")
class ProductAlias {

    private String id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String name;
    private ProductDict productDict;

}