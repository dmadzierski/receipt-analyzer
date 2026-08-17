package pl.madzierski.daniel.receipt.model;

import lombok.Builder;
import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ReceiptRevisionResolveData(
    String revisionVersion,
    String brand,
    List<ReceiptRevisionResolveDataItem> items,
    List<ReceiptRevisionResolveDataFile> files,
    LocalDateTime payingDate,
    String address,
    ReceiptResolverStrategyType strategy,
    BigDecimal totalPrice
) {
    public record ReceiptRevisionResolveDataItem(
        String name,
        BigDecimal amount,
        BigDecimal unitPrice,
        BigDecimal discount,
        BigDecimal totalPrice,
        Integer position
    ) {
    }

    public record ReceiptRevisionResolveDataFile(
        String name,
        int page,
        String rawData
    ) {
    }
}