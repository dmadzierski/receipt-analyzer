package pl.madzierski.daniel.app.product_dict.model;

import java.util.List;

public record GetProductDictListResponse(
        List<ProductDict> items
) {
    public record ProductDict(
            String id,
            String name,
            List<Alias> aliases
    ) {
        public record Alias(String id, String alias) {
        }
    }
}
