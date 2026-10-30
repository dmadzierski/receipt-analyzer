package pl.madzierski.daniel.receipt.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;

import java.util.UUID;

public record CreateReceiptRequest(
    String name,
    String description,
    @NotNull
    ReceiptResolverStrategyType strategy,
    @NotEmpty
    UUID walletId,
    @NotEmpty
    UUID storeId
) {
}