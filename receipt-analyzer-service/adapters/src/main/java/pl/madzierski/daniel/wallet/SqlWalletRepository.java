package pl.madzierski.daniel.wallet;


import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.madzierski.daniel.event.EventPublisher;
import pl.madzierski.daniel.wallet.event.WalletChanged;

import java.util.UUID;

interface SqlWalletRepository extends JpaRepository<SqlWallet, UUID> {
}

@org.springframework.stereotype.Repository
@AllArgsConstructor
class WalletRepositoryImpl implements WalletRepository {

    private final SqlWalletRepository repository;
    private final EventPublisher eventPublisher;

    @Override
    public Wallet save(Wallet wallet) {
        SqlWallet entity = repository.save(SqlWallet.fromWallet(wallet));
        eventPublisher.publishLatest(entity.getId(), () -> {
            Wallet saved = entity.toWallet();
            return new WalletChanged(saved.getId(), saved.getOwner().id(), saved.getName(), saved.getCreatedDate());
        });
        return entity.toWallet();
    }
}

