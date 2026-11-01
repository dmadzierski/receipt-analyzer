package pl.madzierski.daniel.wallet;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import pl.madzierski.daniel.wallet.model.WalletDto;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface SqlWalletQueryRepository extends WalletQueryRepository, Repository<SqlWallet, UUID> {

    @Query("SELECT new pl.madzierski.daniel.wallet.model.WalletDto(w.id, w.name, w.createdDate) FROM SqlWallet w " +
        "WHERE w.owner.id = :userId")
    Collection<WalletDto> findAllByUser(UUID userId);

    @Query("SELECT new pl.madzierski.daniel.wallet.model.WalletDto(w.id, w.name, w.createdDate) FROM SqlWallet w " +
        "WHERE w.id = :walletId AND w.owner.id = :ownerId")
    Optional<WalletDto> getWalletDetails(UUID walletId, UUID ownerId);

    @Query("SELECT COUNT(w) > 0 FROM SqlWallet w WHERE w.id = :walletId AND w.owner.id = :ownerId")
    boolean isAccessible(UUID walletId, UUID ownerId);

}
