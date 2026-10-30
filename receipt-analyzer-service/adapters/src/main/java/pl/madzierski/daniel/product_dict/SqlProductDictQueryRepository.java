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

import java.util.*;

interface SqlProductDictQueryRepository extends JpaRepository<SqlProductDict, UUID> {
    @Query("SELECT d FROM SqlProductDict d LEFT JOIN FETCH d.aliases LEFT JOIN FETCH d.categories")
    Set<ProductDictWithAliasesAndCategoryProjection> findAllWithCategoryAndAliases();

    long countProductDictEntitiesByCategoriesIdIn(Set<UUID> productCategoryIds);
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
    public List<ProductDto> findAllProducts() {
        return this.sqlProductDictQueryRepository.findAll().stream().map(
                product -> {
                    ProductDto productDto = new ProductDto(
                        product.getId(),
                        product.getName()
                    );
                    product.getAliases().forEach(alias -> productDto.addAlias(new ProductAliasDto(alias.getId(), alias.getName())));
                    product.getCategories().forEach(category -> productDto.addCategory(new ProductCategoryDto(category.getId(), category.getName())));
                    return productDto;
                }
        ).toList();
    }

    @Override
    public Optional<ProductDto> findNameById(UUID id) {
        return this.sqlProductDictQueryRepository.findById(id).map(product-> ProductDto.builder().id(product.getId()).name(product.getName()).build());
    }
}