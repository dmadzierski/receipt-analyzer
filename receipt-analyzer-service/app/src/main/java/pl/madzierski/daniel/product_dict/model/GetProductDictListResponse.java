package pl.madzierski.daniel.product_dict.model;

import java.util.List;
import java.util.UUID;

public record GetProductDictListResponse(
    List<ProductDict> items
) {
    public record ProductDict(
        UUID id,
        String name,
        List<ProductCategory> productCategories,
        List<Alias> aliases
    ) {
        public record ProductCategory(UUID id, String name) {
        }

        public record Alias(UUID id, String alias) {
        }
    }
}
