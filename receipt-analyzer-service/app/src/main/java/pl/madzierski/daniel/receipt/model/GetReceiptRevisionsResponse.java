package pl.madzierski.daniel.receipt.model;

import pl.madzierski.daniel.receipt.scan_resolver.ReceiptResolverStrategyType;

import java.time.LocalDateTime;

public record GetReceiptRevisionsResponse(
    String id,
    ReceiptResolverStrategyType resolver,
    LocalDateTime createdDate,
    String brand,
    Double totalPrice,
    LocalDateTime payingDate,
    String address,
    Boolean isPreferredRevision,
    Boolean isCorrect
) {
    public static GetReceiptRevisionsResponse receiptRevisionMapper(ReceiptRevisionDto revision) {
        return new GetReceiptRevisionsResponse(
            revision.getId(),
            revision.getResolver(),
            revision.getCreatedDate(),
            revision.getBrand(),
            revision.getTotalPrice(),
            revision.getPayingDate(),
            revision.getAddress(),
            revision.getIsPreferredRevision(),
            revision.getIsCorrect()
        );
    }
}