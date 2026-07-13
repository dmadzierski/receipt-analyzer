package pl.madzierski.daniel.product_dict.model;

import java.util.List;

public record GetProductDictListResponse(
    List<ProductDict> items
) {
    public record ProductDict(
        String id,
        String name,
        ProductCategory productCategory,
        List<Alias> aliases
    ) {
        public record ProductCategory(String id, String name) {
        }

        public record Alias(String id, String alias) {
        }
    }
}
