package pl.madzierski.daniel.receipt;

import pl.madzierski.daniel.receipt.model.ReceiptRevisionDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReceiptRevisionQueryRepository {
    List<ReceiptRevisionDto> getRevisionsByReceiptId(UUID receiptId);

    Optional<ReceiptRevisionDto> getRevisionById(UUID revisionId);

}
