package pl.madzierski.daniel.receipt.model;

import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record GetRevisionResponse(
    UUID id,
    ReceiptResolverStrategyType resolver,
    LocalDateTime createdDate,
    BigDecimal totalPrice,
    LocalDateTime paymentDate,
    Boolean isPreferredRevision,
    Boolean isCorrect,
    Set<ItemResponse> items
) {

    public static GetRevisionResponse revisionMapper(ReceiptRevisionDto revision, Collection<ReceiptItemDto> items) {
        if (revision == null) {
            return null;
        }

        Set<ItemResponse> mappedItems = items.stream()
            .map(GetRevisionResponse::itemMapper)
            .collect(Collectors.toSet());

        return new GetRevisionResponse(
            revision.getId(),
            revision.getResolver(),
            revision.getCreatedDate(),
            revision.getTotalPrice(),
            revision.getPaymentDate(),
            revision.getIsPreferredRevision(),
            revision.getIsCorrect(),
            mappedItems
        );
    }

    public static ItemResponse itemMapper(ReceiptItemDto receiptItemEntity) {
        return new ItemResponse(
            receiptItemEntity.getId(),
            receiptItemEntity.getName(),
            receiptItemEntity.getAmount(),
            receiptItemEntity.getUnitPrice(),
            receiptItemEntity.getDiscount(),
            receiptItemEntity.getTotalPrice(),
            receiptItemEntity.getPosition()
        );
    }

    public record ItemResponse(
        UUID id,
        String name,
        BigDecimal amount,
        BigDecimal unitPrice,
        BigDecimal discount,
        BigDecimal totalPrice,
        Integer position
    ) {
    }
}