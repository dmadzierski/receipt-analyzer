package pl.madzierski.daniel.app.product_dict.model;

import java.util.List;

public record UpdateProductDictResponse(
        String id,
        String name,
        List<Alias> aliases
) {
    public record Alias(String id, String alias) {
    }
}

