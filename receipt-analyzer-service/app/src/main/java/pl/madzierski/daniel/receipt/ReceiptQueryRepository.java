package pl.madzierski.daniel.receipt;

import pl.madzierski.daniel.receipt.model.ReceiptDto;
import pl.madzierski.daniel.receipt.projection.WalletReceiptProjection;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ReceiptQueryRepository {

    Collection<WalletReceiptProjection> getWalletReceipts(UUID walletId);

    Optional<ReceiptDto> findById(UUID receiptId);
}
