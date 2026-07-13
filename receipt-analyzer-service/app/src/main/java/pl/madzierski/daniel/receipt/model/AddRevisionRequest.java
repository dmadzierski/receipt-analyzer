package pl.madzierski.daniel.receipt.model;

import java.time.LocalDateTime;
import java.util.Set;

public record AddRevisionRequest(
    String receiptId,
    LocalDateTime createdDate,
    String brand,
    Double totalPrice,
    LocalDateTime payingDate,
    String address,
    Set<AddRevisionItemRequest> items
) {
    public record AddRevisionItemRequest(
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