package pl.madzierski.daniel.product_dict;

import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
class ProductDict {
    private Set<ProductAlias> aliases = new HashSet<>();
    private String id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String name;
    private Set<ProductCategory> productCategories = new HashSet<>();

    public void addAlias(ProductAlias productAlias) {
        this.aliases.add(productAlias);
    }

    public void addProductCategory(ProductCategory productCategory) {
        this.productCategories.add(productCategory);
    }
}
