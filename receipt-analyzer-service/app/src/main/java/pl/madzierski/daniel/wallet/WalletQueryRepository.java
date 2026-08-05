package pl.madzierski.daniel.wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.wallet.model.WalletDto;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface WalletQueryRepository extends JpaRepository<WalletEntity, String> {

    @Query("SELECT new pl.madzierski.daniel.wallet.model.WalletDto(w.id, w.name, w.createdDate) FROM WalletEntity w WHERE w.userSub = :userSub")
    Collection<WalletDto> findAllByUserSub(String userSub);

    @Query("SELECT new pl.madzierski.daniel.wallet.model.WalletDto(w.id, w.name, w.createdDate) FROM WalletEntity w WHERE w.id = :walletId")
    Optional<WalletDto> getWalletDetails(String walletId);

}
