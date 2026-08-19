package pl.madzierski.daniel.receipt.model;

import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public record UpdateRevisionRequest(
    String id,
    ReceiptResolverStrategyType resolver,
    LocalDateTime createdDate,
    String brand,
    BigDecimal totalPrice,
    LocalDateTime paymentDate,
    String address,
    Boolean isPreferredRevision,
    Boolean isCorrect,
    Set<ItemRequest> items
) {

    public record ItemRequest(
        String id,
        String name,
        BigDecimal amount,
        BigDecimal unitPrice,
        BigDecimal discount,
        BigDecimal totalPrice,
        Integer position
    ) {
    }
}