package pl.madzierski.daniel.wallet;

import pl.madzierski.daniel.user.model.UserQuery;
import pl.madzierski.daniel.wallet.model.WalletDto;

import java.util.Collection;
import java.util.Optional;

public interface WalletQueryRepository {

    Collection<WalletDto> findAllByUser(String userId);

    Optional<WalletDto> getWalletDetails(String walletId);

}
