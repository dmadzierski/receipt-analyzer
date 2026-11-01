package pl.madzierski.daniel.receipt;


import pl.madzierski.daniel.user.model.UserQuery;

import java.util.Optional;
import java.util.UUID;

interface ReceiptRevisionRepository {
    ReceiptRevision save(ReceiptRevision revision);

    Optional<ReceiptRevision> findAccessibleWithItems(UUID revisionId, UserQuery user);

    Optional<ReceiptRevision> findAccessible(UUID revisionId, UserQuery user);

    void deleteById(UUID revisionId);

    void deleteAllByReceiptId(UUID receiptId);

    void unsetPreferredInOtherRevisions(UUID receiptId, UUID preferredRevisionId);
}
