package pl.madzierski.daniel.receipt.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record AddRevisionRequest(
    UUID receiptId,
    LocalDateTime createdDate,
    String brand,
    BigDecimal totalPrice,
    LocalDateTime paymentDate,
    String address,
    Set<AddRevisionItemRequest> items
) {
    public record AddRevisionItemRequest(
        UUID id,
        String name,
        BigDecimal amount,
        BigDecimal unitPrice,
        BigDecimal discount,
        BigDecimal totalPrice,
        Integer position,
        UUID originalItemId
    ) {
    }
}