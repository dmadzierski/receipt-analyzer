package pl.madzierski.daniel.app.receipt.model;

import pl.madzierski.daniel.app.receipt.ReceiptEntity;
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionEntity;
import pl.madzierski.daniel.app.receipt.revision.item.ReceiptItemEntity;
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategyType;

import java.time.LocalDateTime;
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
            ReceiptEntity receiptEntity,
            ReceiptRevisionEntity preferredRevisionEntity,
            String fileId
    ) {
        return new GetReceiptDetailsResponse(
                receiptEntity.getId(),
                receiptEntity.getName(),
                receiptEntity.getDescription(),
                preferredRevisionMapper(preferredRevisionEntity),
                receiptRevisionMapper(receiptEntity.getReceiptRevisions()),
                receiptEntity.getCreatedDate(),
                receiptEntity.getModifiedDate(),
                fileId
        );
    }

    public static PreferredRevisionResponse preferredRevisionMapper(ReceiptRevisionEntity revisionEntity) {
        if (revisionEntity == null) {
            return null;
        }

        Set<ItemResponse> mappedItems = null;
        if (revisionEntity.getItems() != null) {
            mappedItems = revisionEntity.getItems().stream()
                    .map(receiptItemEntity -> itemMapper(receiptItemEntity, revisionEntity.getResolver()))
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

    public static ItemResponse itemMapper(ReceiptItemEntity receiptItemEntity, ReceiptResolverStrategyType strategy) {
        String name = receiptItemEntity.getName();
        if (strategy != ReceiptResolverStrategyType.USER && receiptItemEntity.getNameDict() != null) {
            name = receiptItemEntity.getNameDict().getName();
        }
        return new ItemResponse(
                receiptItemEntity.getId(),
                name,
                receiptItemEntity.getAmount(),
                receiptItemEntity.getUnitPrice(),
                receiptItemEntity.getDiscount(),
                receiptItemEntity.getTotalPrice(),
                receiptItemEntity.getPosition()
        );
    }

    public static Set<RevisionResponse> receiptRevisionMapper(Set<ReceiptRevisionEntity> receiptRevisions) {
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
            String payingDate,
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
            String payingDate,
            String address,
            Boolean isPreferredRevision,
            Boolean isCorrect
    ) {
    }

    public record FileResponse(
            String id,
            String path,
            String rawData
    ) {
    }
}