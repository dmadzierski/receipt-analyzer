package pl.madzierski.daniel.receipt.model;

import jakarta.validation.constraints.NotNull;
import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;

import java.time.LocalDateTime;

public record CreateReceiptRequest(
    String name,
    String description,
    LocalDateTime date,
    ReceiptResolverStrategyType strategy,
    @NotNull
    String walletId
) {
}