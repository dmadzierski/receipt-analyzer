package pl.madzierski.daniel.wallet;

import pl.madzierski.daniel.wallet.model.WalletDto;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface WalletQueryRepository {

    Collection<WalletDto> findAllByUser(UUID userId);

    Optional<WalletDto> getWalletDetails(UUID walletId);

}
