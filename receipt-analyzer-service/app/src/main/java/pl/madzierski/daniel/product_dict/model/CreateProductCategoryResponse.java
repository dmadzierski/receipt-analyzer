package pl.madzierski.daniel.product_dict.model;

import java.util.UUID;

public record CreateProductCategoryResponse(
    UUID id,
    String name
) {
}
