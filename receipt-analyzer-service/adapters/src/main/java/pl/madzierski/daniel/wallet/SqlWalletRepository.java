package pl.madzierski.daniel.wallet;


import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.madzierski.daniel.event.EventPublisher;

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
        SqlWallet saved = repository.save(SqlWallet.fromWallet(wallet));
        eventPublisher.publishLatest(saved.getId(), () -> WalletEventMapper.changed(saved.toWallet()));
        return saved.toWallet();
    }
}

