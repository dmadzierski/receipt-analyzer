package pl.madzierski.daniel.receipt.model;

import java.util.UUID;

public record CreateReceiptResponse(
    UUID id,
    String name,
    String description
) {
}