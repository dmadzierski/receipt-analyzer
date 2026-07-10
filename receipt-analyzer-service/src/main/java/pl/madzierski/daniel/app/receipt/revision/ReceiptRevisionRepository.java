package pl.madzierski.daniel.app.receipt.revision;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptRevisionRepository extends JpaRepository<ReceiptRevisionEntity, String> {
    @Query("SELECT r FROM ReceiptRevisionEntity r LEFT JOIN FETCH r.items ri LEFT JOIN FETCH ri.nameDict WHERE r.id = :id")
    Optional<ReceiptRevisionEntity> findReceiptRevisionEntitiesById(String id);

    List<ReceiptRevisionEntity> findReceiptRevisionEntityByReceiptId(String receiptId);
}
