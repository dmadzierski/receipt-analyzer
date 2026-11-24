package pl.madzierski.daniel.digitization.model;

import lombok.Builder;
import pl.madzierski.daniel.digitization.ReceiptResolverStrategyType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record DigitizedReceipt(
    String revisionVersion,
    List<DigitizedReceiptItem> items,
    LocalDateTime paymentDate,
    ReceiptResolverStrategyType strategy,
    BigDecimal totalPrice
) {
    public record DigitizedReceiptItem(
        String name,
        BigDecimal amount,
        BigDecimal unitPrice,
        BigDecimal discount,
        BigDecimal totalPrice,
        Integer position
    ) {
    }
}