package pl.madzierski.daniel.app.product_dict.product_category.model;

import jakarta.validation.constraints.NotBlank;

public record UpdateProductCategoryRequest(
        @NotBlank
        String name
) {
}
