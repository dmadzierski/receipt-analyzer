package pl.madzierski.daniel.receipt;

import pl.madzierski.daniel.receipt.model.ReceiptRevisionDto;

import java.util.List;
import java.util.Optional;

public interface ReceiptRevisionQueryRepository {
    List<ReceiptRevisionDto> getRevisionsByReceiptId(String receiptId);

    Optional<ReceiptRevisionDto> getRevisionById(String revisionId);

}
