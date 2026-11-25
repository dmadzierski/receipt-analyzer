package pl.madzierski.daniel.receipt;

import pl.madzierski.daniel.receipt.model.ReceiptDto;
import pl.madzierski.daniel.receipt.model.WalletReceiptDto;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ReceiptQueryRepository {

    Collection<WalletReceiptDto> getWalletReceipts(UUID walletId);

    Optional<ReceiptDto> findById(UUID receiptId, UUID ownerId);
}
