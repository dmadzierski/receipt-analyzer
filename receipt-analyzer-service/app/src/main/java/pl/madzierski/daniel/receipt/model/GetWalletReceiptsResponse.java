package pl.madzierski.daniel.receipt.model;

import pl.madzierski.daniel.receipt.projection.WalletReceiptProjection;
import pl.madzierski.daniel.store.model.StoreDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record GetWalletReceiptsResponse(
    List<Receipt> receipts
) {

    public GetWalletReceiptsResponse(Collection<WalletReceiptProjection> receipts, Map<UUID, StoreDto> stores) {
        this(receipts.stream()
            .map(receipt -> new Receipt(receipt, receipt.storeId() != null ? stores.get(receipt.storeId()) : null))
            .toList());
    }

    public record Receipt(
        UUID id,
        String name,
        LocalDate paymentDate,
        Boolean isCorrect,
        BigDecimal totalPrice,
        LocalDateTime createdDate,
        StoreDto store
    ) {
        Receipt(WalletReceiptProjection receipt, StoreDto store) {
            this(
                receipt.id(),
                receipt.name(),
                receipt.paymentDate() != null ? receipt.paymentDate().toLocalDate() : null,
                receipt.isCorrect(),
                receipt.totalPrice(),
                receipt.createdDate(),
                store);
        }
    }
}
