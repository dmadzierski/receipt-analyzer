package pl.madzierski.daniel.receipt;

import pl.madzierski.daniel.product_dict.model.ProductDictQuery;

import java.util.Collection;
import java.util.List;

interface ReceiptItemRepository {

    void reassignProductDict(ProductDictQuery dict, List<String> productDictIdList);

    void deleteAllByIdIn(List<String> ids);

    void deleteAllByReceiptId(String receiptId);

    List<ReceiptItem> saveAll(List<ReceiptItem> entities, String receiptRevisionId);

    List<ReceiptItem> findUnmatchedItemsByReceiptId(String revisionId);

    void updateProduct(ProductDictQuery productDictQuery, Collection<String> list);
}
