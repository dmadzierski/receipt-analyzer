package pl.madzierski.daniel.receipt.model;

import pl.madzierski.daniel.file_group.model.FileDto;
import pl.madzierski.daniel.file_group.model.FileGroupDto;
import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;
import pl.madzierski.daniel.store.model.StoreDetailsResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
    List<FileGroupResponse> fileGroups,
    StoreDetailsResponse store
) {

    public record FileGroupResponse(
        String id,
        String fileType,
        boolean isOriginal,
        Set<FileResponse> files
    ) {
        public record FileResponse(
            String id,
            String path,
            Integer partNumber
        ) {
            public static FileResponse fileMapper(FileDto file) {
                return new FileResponse(
                    file.getId(),
                    file.getPath(),
                    file.getPartNumber()
                );
            }
        }

    }

    public static FileGroupResponse fileGroupMapper(FileGroupDto fileGroup) {
        return new FileGroupResponse(
            fileGroup.id(),
            fileGroup.fileType().name(),
            fileGroup.isOriginal(),
            fileGroup.files()
                .stream()
                .map(FileGroupResponse.FileResponse::fileMapper)
                .collect(Collectors.toSet())
        );
    }

    public static GetReceiptDetailsResponse receiptDetailsMapper(
        ReceiptDto receiptEntity,
        ReceiptRevisionDto preferredRevisionEntity,
        List<FileGroupDto> fileGroups,
        Collection<ReceiptItemDto> items,
        Collection<ReceiptRevisionDto> revisions,
        StoreDetailsResponse store
    ) {
        return new GetReceiptDetailsResponse(
            receiptEntity.getId(),
            receiptEntity.getName(),
            receiptEntity.getDescription(),
            preferredRevisionMapper(preferredRevisionEntity, items),
            receiptRevisionMapper(revisions),
            receiptEntity.getCreatedDate(),
            receiptEntity.getModifiedDate(),
            fileGroups.stream()
                .map(GetReceiptDetailsResponse::fileGroupMapper)
                .toList(),
            store
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
            revisionEntity.getTotalPrice(),
            revisionEntity.getPaymentDate(),
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
                    it.getTotalPrice(),
                    it.getPaymentDate(),
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
        BigDecimal amount,
        BigDecimal unitPrice,
        BigDecimal discount,
        BigDecimal totalPrice,
        Integer position
    ) {
    }

    public record PreferredRevisionResponse(
        String id,
        ReceiptResolverStrategyType resolver,
        LocalDateTime createdDate,
        BigDecimal totalPrice,
        LocalDateTime paymentDate,
        Boolean isPreferredRevision,
        Boolean isCorrect,
        Set<ItemResponse> items
    ) {
    }

    public record RevisionResponse(
        String id,
        ReceiptResolverStrategyType resolver,
        LocalDateTime createdDate,
        BigDecimal totalPrice,
        LocalDateTime paymentDate,
        Boolean isPreferredRevision,
        Boolean isCorrect
    ) {
    }

}