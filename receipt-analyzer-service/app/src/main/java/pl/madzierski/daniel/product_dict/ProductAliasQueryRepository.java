package pl.madzierski.daniel.product_dict;

import pl.madzierski.daniel.product_dict.model.ProductAliasDto;

import java.util.Set;

public interface ProductAliasQueryRepository {
    Set<ProductAliasDto> findAllAliases();
}
