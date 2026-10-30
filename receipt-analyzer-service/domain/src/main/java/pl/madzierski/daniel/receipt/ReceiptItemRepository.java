package pl.madzierski.daniel.receipt;

import pl.madzierski.daniel.product_dict.model.ProductDictQuery;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

interface ReceiptItemRepository {

    void reassignProductDict(ProductDictQuery dict, List<UUID> productDictIdList);

    void deleteAllByIdIn(List<UUID> ids);

    void deleteAllByReceiptId(UUID receiptId);

    List<ReceiptItem> saveAll(List<ReceiptItem> entities, UUID receiptRevisionId);

    List<ReceiptItem> findUnmatchedItemsByReceiptId(UUID revisionId);

    void updateProduct(ProductDictQuery productDictQuery, Collection<UUID> list);
}
