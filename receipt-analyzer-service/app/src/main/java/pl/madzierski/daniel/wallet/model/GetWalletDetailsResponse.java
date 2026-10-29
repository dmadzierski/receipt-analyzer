package pl.madzierski.daniel.wallet.model;

import pl.madzierski.daniel.store.model.StoreDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record GetWalletDetailsResponse(
    String id,
    String name,
    List<Receipt> receipts
) {

    public record Receipt(
        String id,
        String name,
        LocalDate paymentDate,
        Boolean isCorrect,
        BigDecimal totalPrice,
        LocalDateTime createdDate,
        StoreDto store
    ) {
    }
}
