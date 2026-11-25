package pl.madzierski.daniel.receipt.model;

import java.math.BigDecimal;
import java.util.UUID;

public record ReceiptItemDto(
    UUID id,
    UUID productDictId,
    String name,
    BigDecimal amount,
    BigDecimal unitPrice,
    BigDecimal discount,
    BigDecimal totalPrice,
    Integer position
) {
}
