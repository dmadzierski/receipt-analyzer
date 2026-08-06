package pl.madzierski.daniel.product_dict;

import java.util.List;

interface ProductAliasRepository {

    void reassignAliasesToProductDict(String targetProductDictId, List<String> productDictIdsToMerge);
}
