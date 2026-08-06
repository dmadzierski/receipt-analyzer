package pl.madzierski.daniel.wallet;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import pl.madzierski.daniel.wallet.model.WalletDto;

import java.util.Collection;
import java.util.Optional;

public interface SqlWalletQueryRepository extends WalletQueryRepository, Repository<SqlWallet, String> {

    @Query("SELECT new pl.madzierski.daniel.wallet.model.WalletDto(w.id, w.name, w.createdDate) FROM SqlWallet w WHERE w.userSub = :userSub")
    Collection<WalletDto> findAllByUserSub(String userSub);

    @Query("SELECT new pl.madzierski.daniel.wallet.model.WalletDto(w.id, w.name, w.createdDate) FROM SqlWallet w WHERE w.id = :walletId")
    Optional<WalletDto> getWalletDetails(String walletId);

}
