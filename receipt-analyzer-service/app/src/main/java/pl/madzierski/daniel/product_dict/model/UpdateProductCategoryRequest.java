package pl.madzierski.daniel.product_dict.model;

import jakarta.validation.constraints.NotNull;

public record UpdateProductCategoryRequest(
    @NotNull
    Long version,
    String name
) {
}
