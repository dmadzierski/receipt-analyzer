package pl.madzierski.daniel.receipt.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.UUID;

public record UpdateReceiptRequest(
    @NotBlank
    String name,
    String description,
    @NotEmpty
    UUID storeId
) {
}
