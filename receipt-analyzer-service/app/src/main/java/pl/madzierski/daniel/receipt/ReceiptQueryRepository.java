package pl.madzierski.daniel.receipt;

import pl.madzierski.daniel.receipt.model.ReceiptDto;
import pl.madzierski.daniel.receipt.projection.WalletReceiptProjection;

import java.util.Collection;
import java.util.Optional;

public interface ReceiptQueryRepository {

    Collection<WalletReceiptProjection> getWalletReceipts(String walletId);

    Optional<ReceiptDto> findById(String receiptId);
}
