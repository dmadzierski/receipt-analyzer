package pl.madzierski.daniel.receipt.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record WalletReceiptDto(
    UUID id,
    String name,
    LocalDateTime paymentDate,
    Boolean isCorrect,
    BigDecimal totalPrice,
    LocalDateTime createdDate,
    UUID storeId
) {
}
