package pl.madzierski.daniel.product_dict.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class ProductCategoryDto {
    private String id;
    private String name;
}
