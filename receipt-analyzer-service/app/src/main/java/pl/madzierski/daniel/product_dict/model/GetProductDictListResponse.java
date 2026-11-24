package pl.madzierski.daniel.product_dict.model;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record GetProductDictListResponse(
    List<ProductDict> items
) {
    public GetProductDictListResponse(Collection<ProductDictDto> products) {
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
        ProductDict(ProductDictDto product) {
            this(
                product.id(),
                product.version(),
                product.name(),
                product.categories()
                    .stream()
                    .map(category -> new ProductCategory(category.id(), category.name()))
                    .toList(),
                product.aliases()
                    .stream()
                    .map(alias -> new Alias(alias.id(), alias.name()))
                    .toList());
        }

        public record ProductCategory(UUID id, String name) {
        }

        public record Alias(UUID id, String alias) {
        }
    }
}
