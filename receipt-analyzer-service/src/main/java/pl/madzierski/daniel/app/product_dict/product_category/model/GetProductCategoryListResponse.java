package pl.madzierski.daniel.app.product_dict.product_category.model;

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
