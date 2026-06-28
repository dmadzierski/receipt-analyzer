package pl.madzierski.daniel.app.receipt.revision.model;

import pl.madzierski.daniel.app.file_group.file.FileEntity;
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionEntity;
import pl.madzierski.daniel.app.receipt.revision.item.ItemEntity;
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategyType;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record GetRevisionResponse(
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

    public static GetRevisionResponse revisionMapper(ReceiptRevisionEntity revisionEntity) {
        if (revisionEntity == null) {
            return null;
        }

        Set<ItemResponse> mappedItems = revisionEntity.getItems().stream()
                .map(GetRevisionResponse::itemMapper)
                .collect(Collectors.toSet());

        return new GetRevisionResponse(
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

    public static FileResponse fileMapper(FileEntity file) {
        return new FileResponse(
                file.getId(),
                file.getPath(),
                file.getRawData()
        );
    }

    public static ItemResponse itemMapper(ItemEntity itemEntity) {
        return new ItemResponse(
                itemEntity.getId(),
                itemEntity.getName(),
                itemEntity.getAmount(),
                itemEntity.getUnitPrice(),
                itemEntity.getDiscount(),
                itemEntity.getTotalPrice(),
                itemEntity.getPosition()
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

    public record FileResponse(
            String id,
            String path,
            String rawData
    ) {
    }
}