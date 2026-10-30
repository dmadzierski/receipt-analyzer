package pl.madzierski.daniel.product_dict.model;

import java.util.UUID;

public record UpdateProductCategoryResponse(
    UUID id,
    String name
) {
}
