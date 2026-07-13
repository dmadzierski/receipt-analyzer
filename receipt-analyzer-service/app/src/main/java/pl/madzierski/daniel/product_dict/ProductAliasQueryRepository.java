package pl.madzierski.daniel.product_dict;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.madzierski.daniel.product_dict.model.ProductAliasDto;

import java.util.Set;

public interface ProductAliasQueryRepository extends JpaRepository<ProductAliasEntity, String> {

    @Query("SELECT alias FROM ProductAliasEntity alias")
    Set<ProductAliasDto> findAllAliases();
}
