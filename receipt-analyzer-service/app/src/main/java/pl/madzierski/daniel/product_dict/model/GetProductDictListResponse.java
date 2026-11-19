package pl.madzierski.daniel.product_dict.model;

import pl.madzierski.daniel.product_dict.projection.ProductDictWithAliasesAndCategoryProjection;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record GetProductDictListResponse(
    List<ProductDict> items
) {
    public GetProductDictListResponse(Set<ProductDictWithAliasesAndCategoryProjection> products) {
        this(products.stream()
            .map(ProductDict::new)
            .toList());
    }

    public record ProductDict(
        UUID id,
        long version,
        String name,
        List<ProductCategory> productCategories,
        List<Alias> aliases
    ) {
        ProductDict(ProductDictWithAliasesAndCategoryProjection product) {
            this(
                product.getId(),
                product.getVersion(),
                product.getName(),
                product.getCategories()
                    .stream()
                    .map(category -> new ProductCategory(category.getId(), category.getName()))
                    .toList(),
                product.getAliases()
                    .stream()
                    .map(alias -> new Alias(alias.getId(), alias.getName()))
                    .toList());
        }

        public record ProductCategory(UUID id, String name) {
        }

        public record Alias(UUID id, String alias) {
        }
    }
}
