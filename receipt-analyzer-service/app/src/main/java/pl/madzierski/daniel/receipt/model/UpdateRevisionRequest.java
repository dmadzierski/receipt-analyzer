package pl.madzierski.daniel.receipt.model;

import pl.madzierski.daniel.receipt.scan_resolver.ReceiptResolverStrategyType;

import java.time.LocalDateTime;
import java.util.Set;

public record UpdateRevisionRequest(
    String id,
    ReceiptResolverStrategyType resolver,
    LocalDateTime createdDate,
    String brand,
    Double totalPrice,
    LocalDateTime payingDate,
    String address,
    Boolean isPreferredRevision,
    Boolean isCorrect,
    Set<ItemRequest> items
) {

    public record ItemRequest(
        String id,
        String name,
        Double amount,
        Double unitPrice,
        Double discount,
        Double totalPrice,
        Integer position
    ) {
    }
}