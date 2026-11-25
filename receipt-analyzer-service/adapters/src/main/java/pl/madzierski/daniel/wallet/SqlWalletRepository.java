package pl.madzierski.daniel.wallet;


import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.madzierski.daniel.event.EventPublisher;
import pl.madzierski.daniel.user.model.UserQuery;

import java.util.UUID;

interface SqlWalletRepository extends JpaRepository<SqlWallet, UUID> {
    @Query("SELECT COUNT(w) > 0 FROM SqlWallet w WHERE w.id = :walletId AND w.owner.id = :ownerId")
    boolean isAccessible(UUID walletId, UUID ownerId);
}

@org.springframework.stereotype.Repository
@AllArgsConstructor
class WalletRepositoryImpl implements WalletRepository {

    private final SqlWalletRepository repository;
    private final EventPublisher eventPublisher;

    @Override
    public Wallet save(Wallet wallet) {
        SqlWallet saved = repository.save(SqlWallet.fromWallet(wallet));
        eventPublisher.publishLatest(saved.getId(), () -> WalletEventMapper.changed(saved.toWallet()));
        return saved.toWallet();
    }

    @Override
    public boolean isAccessible(UUID walletId, UserQuery user) {
        return repository.isAccessible(walletId, user.id());
    }
}

