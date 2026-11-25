package pl.madzierski.daniel.receipt;

import pl.madzierski.daniel.user.model.UserQuery;

import java.util.Optional;
import java.util.UUID;

interface ReceiptRepository {
    Receipt save(Receipt receipt);

    Optional<Receipt> findAccessible(UUID receiptId, UserQuery user);

    void deleteById(UUID receiptId);
}
