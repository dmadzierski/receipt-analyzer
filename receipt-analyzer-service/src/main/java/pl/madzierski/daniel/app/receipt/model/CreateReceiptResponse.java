package pl.madzierski.daniel.app.receipt.model;

public record CreateReceiptResponse(
        String id,
        String name,
        String description
) {
}