package pl.madzierski.daniel.receipt;

import lombok.AllArgsConstructor;
import pl.madzierski.daniel.file_group.ReceiptLookup;
import pl.madzierski.daniel.user.model.UserQuery;

import java.util.UUID;

@AllArgsConstructor
class ReceiptLookupImpl implements ReceiptLookup {

    private final ReceiptRepository receiptRepository;
    private final ReceiptRevisionRepository revisionRepository;

    @Override
    public boolean isReceiptAccessible(UUID receiptId, UserQuery user) {
        return receiptRepository.findAccessible(receiptId, user).isPresent();
    }

    @Override
    public boolean isSourceOfRevision(UUID fileGroupId) {
        return revisionRepository.isSourceFileGroup(fileGroupId);
    }
}
