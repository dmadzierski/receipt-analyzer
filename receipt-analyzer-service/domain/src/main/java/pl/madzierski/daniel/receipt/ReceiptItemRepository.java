package pl.madzierski.daniel.receipt;

import pl.madzierski.daniel.product_dict.model.ProductDictQuery;

import java.util.List;
import java.util.UUID;

interface ReceiptItemRepository {

    void reassignProductDict(ProductDictQuery dict, List<UUID> productDictIdList);
}
