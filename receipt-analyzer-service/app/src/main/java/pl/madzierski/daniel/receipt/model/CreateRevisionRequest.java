package pl.madzierski.daniel.receipt.model;

import pl.madzierski.daniel.digitization.ReceiptResolverStrategyType;

import java.util.UUID;

public record CreateRevisionRequest(
    UUID fileGroupId,
    ReceiptResolverStrategyType strategy
) {
}
