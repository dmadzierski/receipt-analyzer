package pl.madzierski.daniel.receipt;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

interface SqlReceiptRepository extends JpaRepository<SqlReceipt, UUID> {
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
    public Optional<Receipt> findById(UUID receiptId) {
        return this.repository.findById(receiptId).map(SqlReceipt::toReceipt);
    }

    @Override
    public void deleteById(UUID receiptId) {
        this.repository.deleteById(receiptId);
    }
}
