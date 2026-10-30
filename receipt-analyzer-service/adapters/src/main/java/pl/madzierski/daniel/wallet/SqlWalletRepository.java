package pl.madzierski.daniel.wallet;


import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SqlWalletRepository extends JpaRepository<SqlWallet, UUID> {
}

@org.springframework.stereotype.Repository
@AllArgsConstructor
class WalletRepositoryImpl implements WalletRepository {

    private final SqlWalletRepository repository;

    @Override
    public Wallet save(Wallet wallet) {
        return repository.save(SqlWallet.fromWallet(wallet)).toWallet();
    }
}

