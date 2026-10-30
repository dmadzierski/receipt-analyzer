package pl.madzierski.daniel.receipt;

import pl.madzierski.daniel.receipt.model.ReceiptItemDto;

import java.util.List;
import java.util.UUID;

public interface ReceiptItemQueryRepository {
    List<ReceiptItemDto> findReceiptItemsByRevisionId(UUID revisionId);

    List<ReceiptItemDto> findAllMissingAliasesInRevision(UUID revisionId);
}
