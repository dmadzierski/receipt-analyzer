package pl.madzierski.daniel.receipt.model;

import jakarta.validation.constraints.NotNull;
import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record UpdateRevisionRequest(
    UUID id,
    @NotNull
    Long version,
    ReceiptResolverStrategyType resolver,
    LocalDateTime createdDate,
    BigDecimal totalPrice,
    LocalDateTime paymentDate,
    Boolean isPreferredRevision,
    Boolean isCorrect,
    @NotNull
    Set<ItemRequest> items
) {

    public record ItemRequest(
        UUID id,
        String name,
        BigDecimal amount,
        BigDecimal unitPrice,
        BigDecimal discount,
        BigDecimal totalPrice,
        Integer position,
        UUID productId
    ) {
    }
}