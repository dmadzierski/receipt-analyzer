package pl.madzierski.daniel.app.receipt.revision.model;

import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionEntity;
import pl.madzierski.daniel.app.receipt.revision.item.ItemEntity;
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategyType;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record AddRevisionResponse(
        String revision,
        ReceiptResolverStrategyType resolver,
        String receiptId,
        LocalDateTime createdDate,
        String brand,
        Double totalPrice,
        String payingDate,
        String address,
        Set<AddRevisionItemResponse> items
) {
    public static AddRevisionResponse addRevisionMapper(ReceiptRevisionEntity receiptRevisionEntity) {
        return new AddRevisionResponse(
                receiptRevisionEntity.getId(),
                receiptRevisionEntity.getResolver(),
                receiptRevisionEntity.getReceipt().getId(),
                receiptRevisionEntity.getCreatedDate(),
                receiptRevisionEntity.getBrand(),
                receiptRevisionEntity.getTotalPrice(),
                receiptRevisionEntity.getPayingDate(),
                receiptRevisionEntity.getAddress(),
                receiptRevisionEntity.getItems().stream()
                        .map(AddRevisionResponse::addRevisionItemMapper)
                        .collect(Collectors.toSet())
        );
    }

    public static AddRevisionItemResponse addRevisionItemMapper(ItemEntity itemEntity) {
        return new AddRevisionItemResponse(
                itemEntity.getId(),
                itemEntity.getName(),
                itemEntity.getAmount(),
                itemEntity.getUnitPrice(),
                itemEntity.getDiscount(),
                itemEntity.getTotalPrice(),
                itemEntity.getPosition(),
                itemEntity.getParentItem() != null ? itemEntity.getParentItem().getId() : null
        );
    }

    public record AddRevisionItemResponse(
            String id,
            String name,
            Double amount,
            Double unitPrice,
            Double discount,
            Double totalPrice,
            Integer position,
            String originalItemId
    ) {
    }
}