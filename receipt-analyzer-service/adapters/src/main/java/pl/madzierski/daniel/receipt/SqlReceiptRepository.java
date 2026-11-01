package pl.madzierski.daniel.receipt;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
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

    @Override
    public Receipt save(Receipt receipt) {
        return this.repository.save(SqlReceipt.fromReceipt(receipt)).toReceipt();
    }

    @Override
    public Optional<Receipt> findAccessible(UUID receiptId, UserQuery user) {
        return this.repository.findByIdAndOwnerId(receiptId, user.getId()).map(SqlReceipt::toReceipt);
    }

    @Override
    public void deleteById(UUID receiptId) {
        this.repository.deleteById(receiptId);
    }
}
