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

    public GetRevisionResponse(ReceiptRevisionDto revision, Collection<ReceiptItemDto> items) {
        this(
            revision.getId(),
            revision.getResolver(),
            revision.getCreatedDate(),
            revision.getTotalPrice(),
            revision.getPaymentDate(),
            revision.getIsPreferredRevision(),
            revision.getIsCorrect(),
            items.stream()
                .map(ItemResponse::new)
                .collect(Collectors.toSet()));
    }

    public record ItemResponse(
        UUID id,
        String name,
        BigDecimal amount,
        BigDecimal unitPrice,
        BigDecimal discount,
        BigDecimal totalPrice,
        Integer position,
        UUID productId
    ) {
        ItemResponse(ReceiptItemDto item) {
            this(
                item.getId(),
                item.getName(),
                item.getAmount(),
                item.getUnitPrice(),
                item.getDiscount(),
                item.getTotalPrice(),
                item.getPosition(),
                item.getProductDictId());
        }
    }
}
