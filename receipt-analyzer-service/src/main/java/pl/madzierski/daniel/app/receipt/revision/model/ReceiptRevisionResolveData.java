package pl.madzierski.daniel.app.receipt.revision.model;

import lombok.Builder;

import java.util.List;

@Builder
public record ReceiptRevisionResolveData(
        String revisionVersion,
        String brand,
        List<ReceiptRevisionResolveDataItem> items,
        List<ReceiptRevisionResolveDataFile> files
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