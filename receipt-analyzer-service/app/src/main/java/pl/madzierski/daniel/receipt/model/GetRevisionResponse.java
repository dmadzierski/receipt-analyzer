package pl.madzierski.daniel.receipt.model;

import pl.madzierski.daniel.receipt.scan_resolver.ReceiptResolverStrategyType;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public record GetRevisionResponse(
    String id,
    ReceiptResolverStrategyType resolver,
    LocalDateTime createdDate,
    String brand,
    Double totalPrice,
    LocalDateTime payingDate,
    String address,
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
            revision.getBrand(),
            revision.getTotalPrice(),
            revision.getPayingDate(),
            revision.getAddress(),
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