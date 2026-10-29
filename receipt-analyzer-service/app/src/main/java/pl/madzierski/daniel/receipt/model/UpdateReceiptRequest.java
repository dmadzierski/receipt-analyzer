package pl.madzierski.daniel.receipt.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record UpdateReceiptRequest(
    @NotBlank
    String name,
    String description,
    @NotEmpty
    String storeId
) {
}
