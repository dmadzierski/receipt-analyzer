package pl.madzierski.daniel.product_dict;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.madzierski.daniel.product_dict.event.ProductCategoryChanged;
import pl.madzierski.daniel.product_dict.event.ProductDictChanged;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ProductDictEventMapper {

    static ProductCategoryChanged changed(ProductCategory category) {
        return new ProductCategoryChanged(category.getId(), category.getVersion(), category.getName());
    }

    static ProductDictChanged changed(ProductDict product) {
        return new ProductDictChanged(product.getId(), product.getVersion(), product.getName(),
            product.getAliases().stream().map(alias -> new ProductDictChanged.Alias(alias.getId(), alias.getName())).toList(),
            product.getProductCategories().stream().map(ProductCategory::getId).toList());
    }
}
