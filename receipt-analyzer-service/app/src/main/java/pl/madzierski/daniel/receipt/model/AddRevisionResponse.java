package pl.madzierski.daniel.receipt.model;

import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public record AddRevisionResponse(
    String revision,
    ReceiptResolverStrategyType resolver,
    String receiptId,
    LocalDateTime createdDate,
    BigDecimal totalPrice,
    LocalDateTime paymentDate,
    Set<AddRevisionItemResponse> items
) {


    public static AddRevisionItemResponse addRevisionItemMapper(ReceiptItemDto receiptItemEntity) {
        return new AddRevisionItemResponse(
            receiptItemEntity.getId(),
            receiptItemEntity.getName(),
            receiptItemEntity.getAmount(),
            receiptItemEntity.getUnitPrice(),
            receiptItemEntity.getDiscount(),
            receiptItemEntity.getTotalPrice(),
            receiptItemEntity.getPosition(),
            receiptItemEntity.getParentItem() != null ? receiptItemEntity.getParentItem().getId() : null
        );
    }

    public record AddRevisionItemResponse(
        String id,
        String name,
        BigDecimal amount,
        BigDecimal unitPrice,
        BigDecimal discount,
        BigDecimal totalPrice,
        Integer position,
        String originalItemId
    ) {
    }
}