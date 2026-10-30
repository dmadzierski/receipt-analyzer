package pl.madzierski.daniel.receipt.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GetReceiptListResponse(
    List<GetReceiptListItemResponse> items
) {
    public record GetReceiptListItemResponse(
        UUID id,
        String name,
        String description,
        LocalDateTime createDate
    ) {
    }
}