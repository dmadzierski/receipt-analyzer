package pl.madzierski.daniel.receipt.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;

public record CreateReceiptRequest(
    String name,
    String description,
    @NotNull
    ReceiptResolverStrategyType strategy,
    @NotEmpty
    String walletId,
    @NotEmpty
    String storeId
) {
}