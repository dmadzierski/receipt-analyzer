package pl.madzierski.daniel.product_dict.model;

import jakarta.validation.constraints.NotBlank;

public record CreateProductCategoryRequest(
    @NotBlank
    String name
) {
}
