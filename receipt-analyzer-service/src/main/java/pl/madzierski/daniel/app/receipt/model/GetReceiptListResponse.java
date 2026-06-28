package pl.madzierski.daniel.app.receipt.model;

import java.util.List;

public record GetReceiptListResponse(
        List<GetReceiptListItemResponse> items
) {
    public record GetReceiptListItemResponse(
            String id,
            String name,
            String description,
            java.time.LocalDateTime createDate
    ) {
    }
}