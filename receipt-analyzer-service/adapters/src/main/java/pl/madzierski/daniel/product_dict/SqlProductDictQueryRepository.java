package pl.madzierski.daniel.product_dict;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.product_dict.projection.ProductDictWithAliasesAndCategoryProjection;

import java.util.Set;
import java.util.UUID;

interface SqlProductDictQueryRepository extends JpaRepository<SqlProductDict, UUID> {
    @Query("SELECT d FROM SqlProductDict d LEFT JOIN FETCH d.aliases LEFT JOIN FETCH d.categories")
    Set<ProductDictWithAliasesAndCategoryProjection> findAllWithCategoryAndAliases();
}

@RequiredArgsConstructor
@Repository
class ProductDictQueryRepositoryImpl implements ProductDictQueryRepository {

    private final SqlProductDictQueryRepository sqlProductDictQueryRepository;

    @Override
    public Set<ProductDictWithAliasesAndCategoryProjection> findAllWithCategoryAndAliases() {
        return sqlProductDictQueryRepository.findAllWithCategoryAndAliases();
    }
}
