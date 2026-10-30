package pl.madzierski.daniel.wallet.model;

import pl.madzierski.daniel.store.model.StoreDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GetWalletDetailsResponse(
    UUID id,
    String name,
    List<Receipt> receipts
) {

    public record Receipt(
        UUID id,
        String name,
        LocalDate paymentDate,
        Boolean isCorrect,
        BigDecimal totalPrice,
        LocalDateTime createdDate,
        StoreDto store
    ) {
    }
}
