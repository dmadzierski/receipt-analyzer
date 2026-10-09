package pl.madzierski.daniel.product_dict.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@EqualsAndHashCode
public class ProductCategoryDto {
    private String id;
    private String name;
}
