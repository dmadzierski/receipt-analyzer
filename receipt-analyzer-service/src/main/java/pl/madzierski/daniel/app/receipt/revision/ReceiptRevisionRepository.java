package pl.madzierski.daniel.app.receipt.revision;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface ReceiptRevisionRepository extends JpaRepository<ReceiptRevisionEntity, String> {
    List<ReceiptRevisionEntity> findReceiptRevisionEntityByReceiptId(String receiptId);
}
