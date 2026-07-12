package pl.madzierski.daniel.app.product_dict.product_category.model;

import jakarta.validation.constraints.NotBlank;

public record CreateProductCategoryRequest(
        @NotBlank
        String name
) {
}
