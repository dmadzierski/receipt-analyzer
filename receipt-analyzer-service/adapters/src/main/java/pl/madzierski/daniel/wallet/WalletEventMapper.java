package pl.madzierski.daniel.wallet;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.madzierski.daniel.wallet.event.WalletChanged;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class WalletEventMapper {

    static WalletChanged changed(Wallet wallet) {
        return new WalletChanged(wallet.getId(), wallet.getVersion(), wallet.getOwner().id(), wallet.getName(), wallet.getCreatedDate());
    }
}
