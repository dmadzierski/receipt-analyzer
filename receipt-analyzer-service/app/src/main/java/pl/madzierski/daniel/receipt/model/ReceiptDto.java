package pl.madzierski.daniel.receipt.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReceiptDto(
    UUID id,
    long version,
    LocalDateTime createdDate,
    LocalDateTime modifiedDate,
    String name,
    String description,
    UUID walletId,
    UUID storeId
) {
}
