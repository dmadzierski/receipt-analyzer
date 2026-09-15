package pl.madzierski.daniel.product_dict;

import pl.madzierski.daniel.product_dict.model.ProductDto;
import pl.madzierski.daniel.product_dict.projection.ProductDictWithAliasesAndCategoryProjection;

import java.util.List;
import java.util.Set;

public interface ProductDictQueryRepository {
    Set<ProductDictWithAliasesAndCategoryProjection> findAllWithCategoryAndAliases();

    long countProductDictEntitiesByCategoriesIdIn(Set<String> productCategoryIds);

    List<ProductDto> findAllProduct(String alias);
}
