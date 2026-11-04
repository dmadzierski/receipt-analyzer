package pl.madzierski.daniel.product_dict;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.product_dict.model.ProductDto;
import pl.madzierski.daniel.product_dict.projection.ProductDictWithAliasesAndCategoryProjection;

import java.util.*;

interface SqlProductDictQueryRepository extends JpaRepository<SqlProductDict, UUID> {
    @Query("SELECT d FROM SqlProductDict d LEFT JOIN FETCH d.aliases LEFT JOIN FETCH d.categories")
    Set<ProductDictWithAliasesAndCategoryProjection> findAllWithCategoryAndAliases();

    long countProductDictEntitiesByCategoriesIdIn(Set<UUID> productCategoryIds);

    long countByIdIn(Collection<UUID> ids);
}

@RequiredArgsConstructor
@Repository
class ProductDictQueryRepositoryImpl implements ProductDictQueryRepository {

    private final SqlProductDictQueryRepository sqlProductDictQueryRepository;

    @Override
    public Set<ProductDictWithAliasesAndCategoryProjection> findAllWithCategoryAndAliases() {
        return sqlProductDictQueryRepository.findAllWithCategoryAndAliases();
    }

    @Override
    public long countProductDictEntitiesByCategoriesIdIn(Set<UUID> productCategoryIds) {
        return sqlProductDictQueryRepository.countProductDictEntitiesByCategoriesIdIn(productCategoryIds);
    }

    @Override
    public Optional<ProductDto> findNameById(UUID id) {
        return this.sqlProductDictQueryRepository.findById(id).map(product-> ProductDto.builder().id(product.getId()).name(product.getName()).build());
    }

    @Override
    public long countByIdIn(Collection<UUID> ids) {
        return sqlProductDictQueryRepository.countByIdIn(ids);
    }
}