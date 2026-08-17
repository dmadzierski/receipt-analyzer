package pl.madzierski.daniel.receipt.model;

import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public record UpdateRevisionResponse(
    String id,
    ReceiptResolverStrategyType resolver,
    LocalDateTime createdDate,
    String brand,
    BigDecimal totalPrice,
    LocalDateTime payingDate,
    String address,
    Boolean isPreferredRevision,
    Boolean isCorrect,
    Set<ItemResponse> items
) {

    public static UpdateRevisionResponse map(ReceiptRevisionDto revision, Collection<ReceiptItemDto> items) {
        Set<ItemResponse> mappedItems = items.stream()
            .map(UpdateRevisionResponse::itemMapper)
            .collect(Collectors.toSet());
        return new UpdateRevisionResponse(
            revision.getId(),
            revision.getResolver(),
            revision.getCreatedDate(),
            revision.getBrand(),
            revision.getTotalPrice(),
            revision.getPayingDate(),
            revision.getAddress(),
            revision.getIsPreferredRevision(),
            revision.getIsCorrect(),
            mappedItems
        );
    }


    public static ItemResponse itemMapper(ReceiptItemDto item) {
        return new ItemResponse(
            item.getId(),
            item.getName(),
            item.getAmount(),
            item.getUnitPrice(),
            item.getDiscount(),
            item.getTotalPrice(),
            item.getPosition()
        );
    }

    public record ItemResponse(
        String id,
        String name,
        BigDecimal amount,
        BigDecimal unitPrice,
        BigDecimal discount,
        BigDecimal totalPrice,
        Integer position
    ) {
    }
}