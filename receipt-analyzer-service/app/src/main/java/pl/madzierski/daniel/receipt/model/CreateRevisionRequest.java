package pl.madzierski.daniel.receipt.model;

import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;

public record CreateRevisionRequest(
    String fileGroupId,
    ReceiptResolverStrategyType strategy
) {
}
