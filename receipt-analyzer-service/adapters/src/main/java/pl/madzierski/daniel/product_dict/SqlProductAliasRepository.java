package pl.madzierski.daniel.product_dict;

import lombok.AllArgsConstructor;
import org.springframework.data.repository.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

interface SqlProductAliasRepository extends Repository<SqlProductAlias, String> {

    Set<SqlProductAlias> findAllByIdIn(Collection<String> aliasIdList);
}

@org.springframework.stereotype.Repository
@AllArgsConstructor
class ProductAliasRepositoryImpl implements ProductAliasRepository {

    private final SqlProductAliasRepository sqlProductAliasRepository;

    @Override
    public Set<ProductAlias> findAllByIds(Set<String> aliasIdList) {
        return sqlProductAliasRepository.findAllByIdIn(aliasIdList).stream()
            .map(alias -> alias.toProductAlias(alias.getProductDict().toProductDict())).collect(Collectors.toSet());
    }
}