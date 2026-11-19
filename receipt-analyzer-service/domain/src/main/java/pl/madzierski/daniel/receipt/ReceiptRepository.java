package pl.madzierski.daniel.receipt;

import pl.madzierski.daniel.store.model.StoreQuery;
import pl.madzierski.daniel.user.model.UserQuery;
import pl.madzierski.daniel.wallet.model.WalletQuery;

import java.util.Optional;
import java.util.UUID;

interface ReceiptRepository {
    Receipt save(Receipt receipt);

    Optional<Receipt> findAccessible(UUID receiptId, UserQuery user);

    boolean isWalletAccessible(WalletQuery wallet, UserQuery user);

    boolean storeExists(StoreQuery store);

    void deleteById(UUID receiptId);
}
