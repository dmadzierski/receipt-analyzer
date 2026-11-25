package pl.madzierski.daniel.file_group;

import pl.madzierski.daniel.user.model.UserQuery;

import java.util.UUID;

public interface ReceiptLookup {
    boolean isReceiptAccessible(UUID receiptId, UserQuery user);

    boolean isSourceOfRevision(UUID fileGroupId);
}
