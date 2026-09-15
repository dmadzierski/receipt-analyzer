package pl.madzierski.daniel.receipt;


import java.util.Optional;

interface ReceiptRevisionRepository {
    ReceiptRevision save(ReceiptRevision revision);

    Optional<ReceiptRevision> findByIdWithItems(String revisionId);

    Optional<ReceiptRevision> findById(String revisionId);
}
