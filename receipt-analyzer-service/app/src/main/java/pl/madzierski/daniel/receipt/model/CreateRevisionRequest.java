package pl.madzierski.daniel.receipt.model;

import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;

import java.util.UUID;

public record CreateRevisionRequest(
    UUID fileGroupId,
    ReceiptResolverStrategyType strategy
) {
}
