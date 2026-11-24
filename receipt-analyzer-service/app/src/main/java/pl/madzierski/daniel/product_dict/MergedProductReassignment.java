package pl.madzierski.daniel.product_dict;

import pl.madzierski.daniel.product_dict.model.ProductDictQuery;

import java.util.List;
import java.util.UUID;

public interface MergedProductReassignment {
    void reassignToPrimary(ProductDictQuery primaryProduct, List<UUID> mergedProductIds);
}
