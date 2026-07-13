package pl.madzierski.daniel.receipt.model;

public record CreateReceiptResponse(
    String id,
    String name,
    String description
) {
}