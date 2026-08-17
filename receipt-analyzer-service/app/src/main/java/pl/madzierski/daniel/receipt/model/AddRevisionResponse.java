package pl.madzierski.daniel.receipt.model;

import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record AddRevisionResponse(
    String revision,
    ReceiptResolverStrategyType resolver,
    String receiptId,
    LocalDateTime createdDate,
    String brand,
    BigDecimal totalPrice,
    LocalDateTime payingDate,
    String address,
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

    public static AddRevisionResponse addRevisionMapper(String receiptId, ReceiptRevisionDto receipt, List<ReceiptItemDto> items) {
        return new AddRevisionResponse(
            receipt.getId(),
            receipt.getResolver(),
            receiptId,
            receipt.getCreatedDate(),
            receipt.getBrand(),
            receipt.getTotalPrice(),
            receipt.getPayingDate(),
            receipt.getAddress(),
            items.stream()
                .map(AddRevisionResponse::addRevisionItemMapper)
                .collect(Collectors.toSet())
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