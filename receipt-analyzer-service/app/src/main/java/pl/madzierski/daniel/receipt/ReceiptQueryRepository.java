package pl.madzierski.daniel.receipt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.receipt.model.ReceiptDto;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface ReceiptQueryRepository extends JpaRepository<ReceiptEntity, String> {

    @Query(value = "SELECT r FROM ReceiptEntity r WHERE r.wallet.id = :walletId")
    Collection<ReceiptDto> getWalletReceipts(String walletId);

    Optional<ReceiptDto> findReceiptEntityById(String receiptId);
}
