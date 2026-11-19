package pl.madzierski.daniel.receipt;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.store.model.StoreQuery;
import pl.madzierski.daniel.user.model.UserQuery;
import pl.madzierski.daniel.wallet.model.WalletQuery;

import java.util.Optional;
import java.util.UUID;

interface SqlReceiptRepository extends JpaRepository<SqlReceipt, UUID> {
    Optional<SqlReceipt> findByIdAndOwnerId(UUID receiptId, UUID ownerId);

    @Query("SELECT COUNT(w) > 0 FROM SqlWallet w WHERE w.id = :walletId AND w.owner.id = :ownerId")
    boolean isWalletAccessible(UUID walletId, UUID ownerId);

    @Query("SELECT COUNT(s) > 0 FROM SqlStore s WHERE s.id = :storeId")
    boolean storeExists(UUID storeId);
}

@AllArgsConstructor
@Repository
class ReceiptRepositoryImpl implements ReceiptRepository {

    private final SqlReceiptRepository repository;

    @Override
    public Receipt save(Receipt receipt) {
        return this.repository.save(SqlReceipt.fromReceipt(receipt)).toReceipt();
    }

    @Override
    public Optional<Receipt> findAccessible(UUID receiptId, UserQuery user) {
        return this.repository.findByIdAndOwnerId(receiptId, user.id()).map(SqlReceipt::toReceipt);
    }

    @Override
    public boolean isWalletAccessible(WalletQuery wallet, UserQuery user) {
        return this.repository.isWalletAccessible(wallet.id(), user.id());
    }

    @Override
    public boolean storeExists(StoreQuery store) {
        return this.repository.storeExists(store.id());
    }

    @Override
    public void deleteById(UUID receiptId) {
        this.repository.deleteById(receiptId);
    }
}
