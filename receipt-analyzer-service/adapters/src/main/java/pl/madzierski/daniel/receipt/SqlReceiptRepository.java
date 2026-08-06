package pl.madzierski.daniel.receipt;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.wallet.model.WalletQueryEntity;

import java.util.List;

interface SqlReceiptRepository extends JpaRepository<SqlReceipt, String> {
    List<SqlReceipt> wallet(WalletQueryEntity wallet);
}

@AllArgsConstructor
@Repository
class ReceiptRepositoryImpl implements ReceiptRepository {

    private final SqlReceiptRepository repository;

    @Override
    public Receipt save(Receipt receipt) {
        return this.repository.save(SqlReceipt.fromReceipt(receipt)).toReceipt();
    }
}
