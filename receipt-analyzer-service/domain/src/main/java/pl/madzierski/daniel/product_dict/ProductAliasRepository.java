package pl.madzierski.daniel.product_dict;

import java.util.List;
import java.util.Set;
import java.util.UUID;

interface ProductAliasRepository {

    Set<ProductAlias> findAllByIds(Set<UUID> aliasIdList);
}
