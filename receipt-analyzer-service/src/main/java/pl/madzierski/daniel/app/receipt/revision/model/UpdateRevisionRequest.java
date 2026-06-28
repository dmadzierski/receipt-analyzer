package pl.madzierski.daniel.app.receipt.revision.model;

import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategyType;

import java.time.LocalDateTime;
import java.util.Set;

public record UpdateRevisionRequest(
        String id,
        ReceiptResolverStrategyType resolver,
        LocalDateTime createdDate,
        String brand,
        Double totalPrice,
        String payingDate,
        String address,
        Boolean isPreferredRevision,
        Boolean isCorrect,
        Set<ItemResponse> items
) {

    public record ItemResponse(
            String id,
            String name,
            Double amount,
            Double unitPrice,
            Double discount,
            Double totalPrice,
            Integer position
    ) {
    }
}