package pl.madzierski.daniel.wallet;

import pl.madzierski.daniel.user.model.UserQuery;

import java.util.UUID;

interface WalletRepository {
    Wallet save(Wallet wallet);

    boolean isAccessible(UUID walletId, UserQuery user);
}
