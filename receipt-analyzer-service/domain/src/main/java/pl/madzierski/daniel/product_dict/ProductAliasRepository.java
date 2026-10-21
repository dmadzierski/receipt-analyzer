package pl.madzierski.daniel.product_dict;

import java.util.List;
import java.util.Set;

interface ProductAliasRepository {

    Set<ProductAlias> findAllByIds(Set<String> aliasIdList);
}
