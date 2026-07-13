package pl.madzierski.daniel.wallet.model;

import java.time.LocalDateTime;
import java.util.List;

public record GetWalletDetailsResponse(
    String id,
    String name,
    List<GetReceiptItemResponse> receipts
) {

    public record GetReceiptItemResponse(
        String id,
        String name,
        String description,
        LocalDateTime createdDate
    ) {
    }
}
