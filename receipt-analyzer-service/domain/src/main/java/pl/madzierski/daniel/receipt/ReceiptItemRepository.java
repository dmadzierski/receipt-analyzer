package pl.madzierski.daniel.receipt;

import pl.madzierski.daniel.product_dict.model.ProductDictQueryEntity;

import java.util.List;

interface ReceiptItemRepository {

    void reassignProductDict(ProductDictQueryEntity dict, List<String> productDictIdList);

    ReceiptItem save(ReceiptItem newItem);

    void deleteAllByIdIn(List<String> ids);

    <S extends ReceiptItem> List<S> saveAll(Iterable<S> entities);
}
