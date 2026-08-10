package pl.madzierski.daniel.product_dict;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import pl.madzierski.daniel.product_dict.model.ProductAliasDto;

import java.util.Set;

public interface SqlProductAliasQueryRepository extends ProductAliasQueryRepository, Repository<SqlProductAlias, String> {
    @Query("SELECT new pl.madzierski.daniel.product_dict.model.ProductAliasDto(alias.id, alias.name, alias.productDict.id) FROM SqlProductAlias alias")
    Set<ProductAliasDto> findAllAliases();
}
