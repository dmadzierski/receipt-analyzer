package pl.madzierski.daniel.product_dict.model;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record UpdateProductDictListRequest(Collection<UpdateProductDict> items) {

    public record UpdateProductDict(String canonicalName, List<UUID> productCategoryIds,
                                    List<UUID> productDictList, Set<UUID> aliasIds) {
    }
}
