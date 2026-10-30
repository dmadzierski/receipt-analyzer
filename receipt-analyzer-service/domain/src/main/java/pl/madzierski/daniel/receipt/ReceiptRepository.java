package pl.madzierski.daniel.receipt;

import java.util.Optional;
import java.util.UUID;

interface ReceiptRepository {
    Receipt save(Receipt receipt);

    Optional<Receipt> findById(UUID receiptId);

    void deleteById(UUID receiptId);
}
