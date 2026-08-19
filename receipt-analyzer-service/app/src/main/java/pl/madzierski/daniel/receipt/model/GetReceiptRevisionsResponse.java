package pl.madzierski.daniel.receipt.model;

import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GetReceiptRevisionsResponse(
    String id,
    ReceiptResolverStrategyType resolver,
    LocalDateTime createdDate,
    BigDecimal totalPrice,
    LocalDateTime paymentDate,
    Boolean isPreferredRevision,
    Boolean isCorrect
) {
    public static GetReceiptRevisionsResponse receiptRevisionMapper(ReceiptRevisionDto revision) {
        return new GetReceiptRevisionsResponse(
            revision.getId(),
            revision.getResolver(),
            revision.getCreatedDate(),
            revision.getTotalPrice(),
            revision.getPaymentDate(),
            revision.getIsPreferredRevision(),
            revision.getIsCorrect()
        );
    }
}