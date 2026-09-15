package pl.madzierski.daniel.product_dict;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.product_dict.model.ProductDto;
import pl.madzierski.daniel.product_dict.model.ProductAliasDto;
import pl.madzierski.daniel.product_dict.model.ProductCategoryDto;
import pl.madzierski.daniel.product_dict.projection.ProductDictWithAliasesAndCategoryProjection;
import pl.madzierski.daniel.product_dict.projection.SimpleProductProjection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SqlProductDictQueryRepository extends JpaRepository<SqlProductDict, String> {
    @Query("SELECT d FROM SqlProductDict d LEFT JOIN FETCH d.aliases LEFT JOIN FETCH d.categories")
    Set<ProductDictWithAliasesAndCategoryProjection> findAllWithCategoryAndAliases();

    long countProductDictEntitiesByCategoriesIdIn(Set<String> productCategoryIds);

    @Query("""
        SELECT
            d.id AS id,
            d.name AS name,
            a.id AS aliasId,
            a.name AS aliasName,
            c.id AS categoryId,
            c.name AS categoryName
        FROM SqlProductDict d
        LEFT JOIN d.aliases a
        LEFT JOIN d.categories c
        WHERE a.name = :alias
        """)
    List<SimpleProductProjection> findAllProduct(String alias);
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
    public long countProductDictEntitiesByCategoriesIdIn(Set<String> productCategoryIds) {
        return sqlProductDictQueryRepository.countProductDictEntitiesByCategoriesIdIn(productCategoryIds);
    }

    @Override
    public List<ProductDto> findAllProduct(String alias) {
        Map<String, ProductDto> productDtoById = new HashMap<>();
        for (SimpleProductProjection projection : sqlProductDictQueryRepository.findAllProduct(alias)) {
            ProductDto productDto = productDtoById.computeIfAbsent(projection.getId(), id -> ProductDto.builder()
                .id(projection.getId())
                .name(projection.getName())
                .build());
            if (projection.getAliasId() != null) {
                productDto.addAlias(ProductAliasDto.builder()
                    .id(projection.getAliasId())
                    .name(projection.getAliasName())
                    .productDictId(projection.getId())
                    .build());
            }
            if (projection.getCategoryId() != null) {
                productDto.getProductCategories().add(ProductCategoryDto.builder()
                    .id(projection.getCategoryId())
                    .name(projection.getCategoryName())
                    .build());
            }
        }
        return productDtoById.values().stream().toList();
    }
}