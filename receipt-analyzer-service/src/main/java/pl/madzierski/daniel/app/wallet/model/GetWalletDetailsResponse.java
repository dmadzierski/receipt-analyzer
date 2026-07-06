package pl.madzierski.daniel.app.wallet.model;

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
            java.time.LocalDateTime createDate
    ) {
    }
}
