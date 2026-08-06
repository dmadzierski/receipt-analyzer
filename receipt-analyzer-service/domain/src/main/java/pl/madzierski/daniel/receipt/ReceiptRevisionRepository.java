package pl.madzierski.daniel.receipt;


import java.util.Optional;

interface ReceiptRevisionRepository {
    ReceiptRevision save(ReceiptRevision revision);

    Optional<ReceiptRevision> findById(String revisionId);
}
