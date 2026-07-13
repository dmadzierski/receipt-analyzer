package pl.madzierski.daniel.product_dict.model;

import java.util.Collection;
import java.util.List;

public record UpdateProductDictListRequest(Collection<UpdateProductDict> items) {

    public record UpdateProductDict(String canonicalName, String productCategoryId, List<String> productDictList) {
    }
}
