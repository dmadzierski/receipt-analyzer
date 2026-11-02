package pl.madzierski.daniel.receipt.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateReceiptRequest(
    @NotBlank
    String name,
    String description,
    @NotNull
    UUID storeId
) {
}
