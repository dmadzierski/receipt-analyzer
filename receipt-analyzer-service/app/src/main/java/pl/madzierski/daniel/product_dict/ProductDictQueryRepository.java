package pl.madzierski.daniel.product_dict;

import pl.madzierski.daniel.product_dict.model.ProductDto;
import pl.madzierski.daniel.product_dict.projection.ProductDictWithAliasesAndCategoryProjection;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ProductDictQueryRepository {
    Set<ProductDictWithAliasesAndCategoryProjection> findAllWithCategoryAndAliases();

    long countProductDictEntitiesByCategoriesIdIn(Set<UUID> productCategoryIds);

    List<ProductDto> findAllProducts();

    Optional<ProductDto> findNameById(UUID id);
}
