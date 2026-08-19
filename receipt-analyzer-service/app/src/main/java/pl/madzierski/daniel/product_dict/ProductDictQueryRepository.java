package pl.madzierski.daniel.product_dict;

import pl.madzierski.daniel.product_dict.projection.ProductDictWithAliasesAndCategoryProjection;

import java.util.Set;

public interface ProductDictQueryRepository {
    Set<ProductDictWithAliasesAndCategoryProjection> findAllWithCategoryAndAliases();

    long countProductDictEntitiesByCategoriesIdIn(Set<String> productCategoryIds);
}
