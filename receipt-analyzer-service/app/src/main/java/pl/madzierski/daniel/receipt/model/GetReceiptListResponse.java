package pl.madzierski.daniel.receipt.model;

import java.time.LocalDateTime;
import java.util.List;

public record GetReceiptListResponse(
    List<GetReceiptListItemResponse> items
) {
    public record GetReceiptListItemResponse(
        String id,
        String name,
        String description,
        LocalDateTime createDate
    ) {
    }
}