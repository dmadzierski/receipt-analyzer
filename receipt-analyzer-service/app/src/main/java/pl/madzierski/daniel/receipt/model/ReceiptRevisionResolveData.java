package pl.madzierski.daniel.receipt.model;

import lombok.Builder;
import pl.madzierski.daniel.receipt.scan_resolver.ReceiptResolverStrategyType;

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
    Double totalPrice
) {
    public record ReceiptRevisionResolveDataItem(
        String name,
        Double amount,
        Double unitPrice,
        Double discount,
        Double totalPrice,
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