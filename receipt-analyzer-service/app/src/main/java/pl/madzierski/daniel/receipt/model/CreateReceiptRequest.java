package pl.madzierski.daniel.receipt.model;

import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;

import java.time.LocalDateTime;

public record CreateReceiptRequest(
    String name,
    String description,
    LocalDateTime date,
    ReceiptResolverStrategyType strategy,
    String walletId
) {
}