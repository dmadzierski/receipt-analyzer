package pl.madzierski.daniel.receipt;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.event.EventPublisher;
import pl.madzierski.daniel.receipt.event.ReceiptChanged;
import pl.madzierski.daniel.receipt.event.ReceiptDeleted;
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
    private final EventPublisher eventPublisher;

    @Override
    public Receipt save(Receipt receipt) {
        SqlReceipt entity = this.repository.save(SqlReceipt.fromReceipt(receipt));
        eventPublisher.publishLatest(entity.getId(), () -> {
            Receipt saved = entity.toReceipt();
            return new ReceiptChanged(saved.getId(), saved.getOwner() != null ? saved.getOwner().id() : null,
            saved.getWallet() != null ? saved.getWallet().id() : null, saved.getStore() != null ? saved.getStore().id() : null,
            saved.getName(), saved.getDescription(), saved.getCreatedDate(), saved.getModifiedDate(),
            saved.getPreferredRevision() != null ? saved.getPreferredRevision().id() : null);
        });
        return entity.toReceipt();
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
        eventPublisher.publish(new ReceiptDeleted(receiptId));
    }
}
