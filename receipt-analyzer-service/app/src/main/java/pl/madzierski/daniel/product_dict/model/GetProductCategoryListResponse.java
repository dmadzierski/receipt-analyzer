package pl.madzierski.daniel.product_dict.model;

import java.util.List;
import java.util.UUID;

public record GetProductCategoryListResponse(
    List<ProductCategory> items
) {
    public record ProductCategory(
        UUID id,
        String name
    ) {
    }
}
