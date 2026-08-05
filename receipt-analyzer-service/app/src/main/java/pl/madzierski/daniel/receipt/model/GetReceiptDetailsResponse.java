package pl.madzierski.daniel.receipt.model;

import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public record GetReceiptDetailsResponse(
    String id,
    String name,
    String description,
    PreferredRevisionResponse preferredRevision,
    Set<RevisionResponse> revisions,
    LocalDateTime createdDate,
    LocalDateTime updateDate,
    String fileId
) {
    public static GetReceiptDetailsResponse receiptDetailsMapper(
        ReceiptDto receiptEntity,
        ReceiptRevisionDto preferredRevisionEntity,
        String fileId,
        Collection<ReceiptItemDto> items,
        Collection<ReceiptRevisionDto> revisions
    ) {
        return new GetReceiptDetailsResponse(
            receiptEntity.getId(),
            receiptEntity.getName(),
            receiptEntity.getDescription(),
            preferredRevisionMapper(preferredRevisionEntity, items),
            receiptRevisionMapper(revisions),
            receiptEntity.getCreatedDate(),
            receiptEntity.getModifiedDate(),
            fileId
        );
    }

    public static PreferredRevisionResponse preferredRevisionMapper(ReceiptRevisionDto revisionEntity, Collection<ReceiptItemDto> items) {
        if (revisionEntity == null) {
            return null;
        }

        Set<ItemResponse> mappedItems = null;
        if (items != null) {
            mappedItems = items.stream()
                .map(GetReceiptDetailsResponse::itemMapper)
                .collect(Collectors.toSet());
        }

        return new PreferredRevisionResponse(
            revisionEntity.getId(),
            revisionEntity.getResolver(),
            revisionEntity.getCreatedDate(),
            revisionEntity.getBrand(),
            revisionEntity.getTotalPrice(),
            revisionEntity.getPayingDate(),
            revisionEntity.getAddress(),
            revisionEntity.getIsPreferredRevision(),
            revisionEntity.getIsCorrect(),
            mappedItems
        );
    }

    public static ItemResponse itemMapper(ReceiptItemDto receiptItem) {
        String name = receiptItem.getName();
        return new ItemResponse(
            receiptItem.getId(),
            name,
            receiptItem.getAmount(),
            receiptItem.getUnitPrice(),
            receiptItem.getDiscount(),
            receiptItem.getTotalPrice(),
            receiptItem.getPosition()
        );
    }

    public static Set<RevisionResponse> receiptRevisionMapper(Collection<ReceiptRevisionDto> receiptRevisions) {
        if (receiptRevisions != null && !receiptRevisions.isEmpty()) {
            return receiptRevisions.stream()
                .map(it -> new RevisionResponse(
                    it.getId(),
                    it.getResolver(),
                    it.getCreatedDate(),
                    it.getBrand(),
                    it.getTotalPrice(),
                    it.getPayingDate(),
                    it.getAddress(),
                    it.getIsPreferredRevision(),
                    it.getIsCorrect()
                ))
                .collect(Collectors.toSet());
        }
        return Collections.emptySet();
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

    public record PreferredRevisionResponse(
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
    }

    public record RevisionResponse(
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
    }

}