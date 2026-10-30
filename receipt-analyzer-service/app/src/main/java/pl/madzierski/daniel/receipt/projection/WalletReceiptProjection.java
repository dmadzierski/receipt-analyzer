package pl.madzierski.daniel.receipt.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record WalletReceiptProjection(
    UUID id,
    String name,
    LocalDateTime paymentDate,
    Boolean isCorrect,
    BigDecimal totalPrice,
    LocalDateTime createdDate,
    UUID storeId
) {
}
