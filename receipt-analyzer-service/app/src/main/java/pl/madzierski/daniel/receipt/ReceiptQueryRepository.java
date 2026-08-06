package pl.madzierski.daniel.receipt;

import pl.madzierski.daniel.receipt.model.ReceiptDto;

import java.util.Collection;
import java.util.Optional;

public interface ReceiptQueryRepository {
    Collection<ReceiptDto> getWalletReceipts(String walletId);

    Optional<ReceiptDto> findReceiptEntityById(String receiptId);
}
