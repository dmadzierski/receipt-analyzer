package pl.madzierski.daniel.product_dict.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@EqualsAndHashCode
public class ProductAliasDto {
    private String id;
    private String name;
    private String productDictId;
}
