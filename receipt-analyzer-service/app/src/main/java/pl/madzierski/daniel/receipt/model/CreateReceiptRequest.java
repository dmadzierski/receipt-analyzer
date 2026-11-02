package pl.madzierski.daniel.receipt.model;

import jakarta.validation.constraints.NotNull;
import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;

import java.util.UUID;

public record CreateReceiptRequest(
    String name,
    String description,
    @NotNull
    ReceiptResolverStrategyType strategy,
    @NotNull
    UUID walletId,
    @NotNull
    UUID storeId
) {
}