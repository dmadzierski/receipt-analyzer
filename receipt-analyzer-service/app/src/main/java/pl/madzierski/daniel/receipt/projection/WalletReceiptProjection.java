package pl.madzierski.daniel.receipt.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletReceiptProjection(
    String id,
    String name,
    LocalDateTime paymentDate,
    Boolean isCorrect,
    BigDecimal totalPrice,
    LocalDateTime createdDate,
    String storeId
) {
}
