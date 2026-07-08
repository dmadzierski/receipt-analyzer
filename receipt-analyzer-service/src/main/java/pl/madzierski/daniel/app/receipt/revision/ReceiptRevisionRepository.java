package pl.madzierski.daniel.app.receipt.revision;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceiptRevisionRepository extends JpaRepository<ReceiptRevisionEntity, String> {
    @Query("SELECT r FROM ReceiptRevisionEntity r LEFT JOIN FETCH r.items ri WHERE r.id = :id")
    ReceiptRevisionEntity findReceiptRevisionEntitiesById(String id);

    List<ReceiptRevisionEntity> findReceiptRevisionEntityByReceiptId(String receiptId);
}
