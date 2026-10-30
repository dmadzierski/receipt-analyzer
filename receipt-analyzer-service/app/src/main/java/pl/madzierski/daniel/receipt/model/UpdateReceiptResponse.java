package pl.madzierski.daniel.receipt.model;

import java.util.UUID;

public record UpdateReceiptResponse(
    UUID id,
    String name,
    String description,
    UUID storeId
) {
}
