package pl.madzierski.daniel.receipt;

import java.util.Optional;

interface ReceiptRepository {
    Receipt save(Receipt receipt);

    Optional<Receipt> findById(String receiptId);
}
