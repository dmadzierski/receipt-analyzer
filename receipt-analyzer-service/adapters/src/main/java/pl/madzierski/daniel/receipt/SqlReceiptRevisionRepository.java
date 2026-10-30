package pl.madzierski.daniel.receipt;


import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

interface SqlReceiptRevisionRepository extends JpaRepository<SqlReceiptRevision, UUID> {
    @Query("SELECT r FROM SqlReceiptRevision r LEFT JOIN FETCH r.items item LEFT JOIN FETCH item.parentItem parentItem WHERE r.id = :revisionId")
    Optional<SqlReceiptRevision> findByIdWithItems(UUID revisionId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        UPDATE SqlReceiptRevision r
        SET r.parentReceiptRevision = null, r.sourceFileGroup = null
        WHERE r.receipt.id = :receiptId
        """)
    void detachReferencesByReceiptId(UUID receiptId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM SqlReceiptRevision r WHERE r.receipt.id = :receiptId")
    void deleteByReceiptId(UUID receiptId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        UPDATE SqlReceiptRevision r
        SET r.isPreferredRevision = false
        WHERE r.receipt.id = :receiptId AND r.id <> :preferredRevisionId AND r.isPreferredRevision = true
        """)
    void unsetPreferredInOtherRevisions(UUID receiptId, UUID preferredRevisionId);
}

@AllArgsConstructor
@Repository
class ReceiptRevisionRepositoryImpl implements ReceiptRevisionRepository {
    private final SqlReceiptRevisionRepository repository;

    @Override
    public ReceiptRevision save(ReceiptRevision revision) {
        return repository.save(SqlReceiptRevision.fromReceiptRevision(revision))
            .toReceiptRevision();
    }

    @Override
    public Optional<ReceiptRevision> findByIdWithItems(UUID revisionId) {
        return repository.findByIdWithItems(revisionId)
            .map(SqlReceiptRevision::toReceiptRevision);
    }

    @Override
    public Optional<ReceiptRevision> findById(UUID revisionId) {
        return repository.findById(revisionId)
            .map(SqlReceiptRevision::toReceiptRevision);
    }

    @Override
    public void deleteById(UUID revisionId) {
        repository.deleteById(revisionId);
    }

    @Override
    public void deleteAllByReceiptId(UUID receiptId) {
        repository.detachReferencesByReceiptId(receiptId);
        repository.deleteByReceiptId(receiptId);
    }

    @Override
    public void unsetPreferredInOtherRevisions(UUID receiptId, UUID preferredRevisionId) {
        repository.unsetPreferredInOtherRevisions(receiptId, preferredRevisionId);
    }
}
