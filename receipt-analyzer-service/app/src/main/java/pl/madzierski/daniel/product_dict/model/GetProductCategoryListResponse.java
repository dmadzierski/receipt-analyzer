package pl.madzierski.daniel.product_dict.model;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record GetProductCategoryListResponse(
    List<ProductCategory> items
) {

    public GetProductCategoryListResponse(Collection<ProductCategoryDto> categories) {
        this(categories.stream().map(ProductCategory::new).toList());
    }

    public record ProductCategory(
        UUID id,
        long version,
        String name
    ) {
        ProductCategory(ProductCategoryDto category) {
            this(category.getId(), category.getVersion(), category.getName());
        }
    }
}
