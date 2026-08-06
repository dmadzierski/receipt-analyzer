package pl.madzierski.daniel.wallet;

import pl.madzierski.daniel.wallet.model.WalletDto;

import java.util.Collection;
import java.util.Optional;

public interface WalletQueryRepository {

    Collection<WalletDto> findAllByUserSub(String userSub);

    Optional<WalletDto> getWalletDetails(String walletId);

}
