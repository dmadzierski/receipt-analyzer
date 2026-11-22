package pl.madzierski.daniel.product_dict.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record UpdateProductDictListRequest(Collection<@Valid UpdateProductDict> items) {

    public record UpdateProductDict(@NotNull Long version, String canonicalName, List<UUID> productCategoryIds,
                                    List<UUID> productDictList, Set<UUID> aliasIds) {
    }
}
