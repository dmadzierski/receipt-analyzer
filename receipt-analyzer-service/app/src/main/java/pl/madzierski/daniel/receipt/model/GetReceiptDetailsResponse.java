package pl.madzierski.daniel.receipt.model;

import pl.madzierski.daniel.file_group.model.FileDto;
import pl.madzierski.daniel.file_group.model.FileGroupDto;
import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;
import pl.madzierski.daniel.store.model.StoreDetailsResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record GetReceiptDetailsResponse(
    UUID id,
    String name,
    String description,
    UUID walletId,
    PreferredRevisionResponse preferredRevision,
    Set<RevisionResponse> revisions,
    LocalDateTime createdDate,
    LocalDateTime updateDate,
    List<FileGroupResponse> fileGroups,
    StoreDetailsResponse store
) {

    public GetReceiptDetailsResponse(ReceiptDto receipt, ReceiptRevisionDto preferredRevision, List<FileGroupDto> fileGroups,
                                     Collection<ReceiptItemDto> items, Collection<ReceiptRevisionDto> revisions,
                                     StoreDetailsResponse store) {
        this(
            receipt.getId(),
            receipt.getName(),
            receipt.getDescription(),
            receipt.getWallet() != null ? receipt.getWallet().id() : null,
            preferredRevision != null ? new PreferredRevisionResponse(preferredRevision, items) : null,
            revisions.stream()
                .map(RevisionResponse::new)
                .collect(Collectors.toSet()),
            receipt.getCreatedDate(),
            receipt.getModifiedDate(),
            fileGroups.stream()
                .map(FileGroupResponse::new)
                .toList(),
            store);
    }

    public record FileGroupResponse(
        UUID id,
        String fileType,
        Set<FileResponse> files
    ) {
        FileGroupResponse(FileGroupDto fileGroup) {
            this(
                fileGroup.id(),
                fileGroup.fileType().name(),
                fileGroup.files()
                    .stream()
                    .map(FileResponse::new)
                    .collect(Collectors.toSet()));
        }

        public record FileResponse(
            UUID id,
            String path,
            Integer partNumber
        ) {
            FileResponse(FileDto file) {
                this(file.getId(), file.getPath(), file.getPartNumber());
            }
        }
    }

    public record PreferredRevisionResponse(
        UUID id,
        ReceiptResolverStrategyType resolver,
        LocalDateTime createdDate,
        BigDecimal totalPrice,
        LocalDateTime paymentDate,
        Boolean isPreferredRevision,
        Boolean isCorrect,
        Set<ItemResponse> items
    ) {
        PreferredRevisionResponse(ReceiptRevisionDto revision, Collection<ReceiptItemDto> items) {
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
    }

    public record RevisionResponse(
        UUID id,
        ReceiptResolverStrategyType resolver,
        LocalDateTime createdDate,
        BigDecimal totalPrice,
        LocalDateTime paymentDate,
        Boolean isPreferredRevision,
        Boolean isCorrect
    ) {
        RevisionResponse(ReceiptRevisionDto revision) {
            this(
                revision.getId(),
                revision.getResolver(),
                revision.getCreatedDate(),
                revision.getTotalPrice(),
                revision.getPaymentDate(),
                revision.getIsPreferredRevision(),
                revision.getIsCorrect());
        }
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
