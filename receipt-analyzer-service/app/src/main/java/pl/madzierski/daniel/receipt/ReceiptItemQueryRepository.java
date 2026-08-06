package pl.madzierski.daniel.receipt;

import pl.madzierski.daniel.receipt.model.ReceiptItemDto;

import java.util.Collection;
import java.util.List;

public interface ReceiptItemQueryRepository {
    Collection<ReceiptItemDto> findReceiptItemsByRevisionId(String revisionId);

    List<ReceiptItemDto> findAllMissingAliasesInRevision(String revisionId);
}
