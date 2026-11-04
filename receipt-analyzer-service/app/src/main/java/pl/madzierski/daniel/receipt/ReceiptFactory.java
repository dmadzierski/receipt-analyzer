package pl.madzierski.daniel.receipt;

import pl.madzierski.daniel.receipt.model.CreateReceiptRequest;
import pl.madzierski.daniel.store.model.StoreQuery;
import pl.madzierski.daniel.user.model.AuthenticatedUser;
import pl.madzierski.daniel.wallet.model.WalletQuery;

class ReceiptFactory {

    Receipt from(CreateReceiptRequest body, AuthenticatedUser user) {
        return Receipt.create(body.name(), body.description(), new WalletQuery(body.walletId()),
            new StoreQuery(body.storeId()), user.toUserQuery());
    }
}
