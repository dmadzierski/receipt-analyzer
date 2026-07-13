package pl.madzierski.daniel.receipt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.wallet.model.WalletQueryEntity;

import java.util.List;

@Repository
interface ReceiptRepository extends JpaRepository<ReceiptEntity, String> {
    List<ReceiptEntity> wallet(WalletQueryEntity wallet);
}
