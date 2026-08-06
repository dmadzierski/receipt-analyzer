package pl.madzierski.daniel.wallet;


import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

interface SqlWalletRepository extends JpaRepository<SqlWallet, String> {
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

