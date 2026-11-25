package pl.madzierski.daniel.receipt;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.event.EventPublisher;
import pl.madzierski.daniel.receipt.event.ReceiptDeleted;
import pl.madzierski.daniel.user.model.UserQuery;

import java.util.Optional;
import java.util.UUID;

interface SqlReceiptRepository extends JpaRepository<SqlReceipt, UUID> {
    Optional<SqlReceipt> findByIdAndOwnerId(UUID receiptId, UUID ownerId);
}

@AllArgsConstructor
@Repository
class ReceiptRepositoryImpl implements ReceiptRepository {

    private final SqlReceiptRepository repository;
    private final EventPublisher eventPublisher;

    @Override
    public Receipt save(Receipt receipt) {
        SqlReceipt saved = this.repository.save(SqlReceipt.fromReceipt(receipt));
        eventPublisher.publishLatest(saved.getId(), () -> ReceiptEventMapper.changed(saved.toReceipt()));
        return saved.toReceipt();
    }

    @Override
    public Optional<Receipt> findAccessible(UUID receiptId, UserQuery user) {
        return this.repository.findByIdAndOwnerId(receiptId, user.id()).map(SqlReceipt::toReceipt);
    }

    @Override
    public void deleteById(UUID receiptId) {
        this.repository.deleteById(receiptId);
        eventPublisher.publish(new ReceiptDeleted(receiptId));
    }
}
