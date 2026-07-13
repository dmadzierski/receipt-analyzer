package pl.madzierski.daniel.product_dict.model;

import java.util.List;

public record GetProductCategoryListResponse(
    List<ProductCategory> items
) {
    public record ProductCategory(
        String id,
        String name
    ) {
    }
}
