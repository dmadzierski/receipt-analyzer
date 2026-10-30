package pl.madzierski.daniel.receipt;


import java.util.Optional;
import java.util.UUID;

interface ReceiptRevisionRepository {
    ReceiptRevision save(ReceiptRevision revision);

    Optional<ReceiptRevision> findByIdWithItems(UUID revisionId);

    Optional<ReceiptRevision> findById(UUID revisionId);

    void deleteById(UUID revisionId);

    void deleteAllByReceiptId(UUID receiptId);

    void unsetPreferredInOtherRevisions(UUID receiptId, UUID preferredRevisionId);
}
