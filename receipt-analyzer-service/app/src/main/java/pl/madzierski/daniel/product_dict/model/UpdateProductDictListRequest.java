package pl.madzierski.daniel.product_dict.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public record UpdateProductDictListRequest(Collection<UpdateProductDict> items) {

    public record UpdateProductDict(String canonicalName, List<String> productCategoryIds,
                                    List<String> productDictList) {
    }
}
