package pl.madzierski.daniel.product_dict;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.madzierski.daniel.product_dict.projection.ProductDictWithAliasesAndCategoryProjection;

import java.util.Set;

public interface ProductDictQueryRepository extends JpaRepository<ProductDictEntity, String> {

    @Query("SELECT d FROM ProductDictEntity d LEFT JOIN FETCH d.aliases LEFT JOIN FETCH d.productCategory")
    Set<ProductDictWithAliasesAndCategoryProjection> findAllWithCategoryAndAliases();

    long countProductDictEntitiesByProductCategoryId(String productCategoryId);
}
