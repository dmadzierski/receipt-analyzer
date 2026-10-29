package pl.madzierski.daniel.receipt.model;

public record UpdateReceiptResponse(
    String id,
    String name,
    String description,
    String storeId
) {
}
