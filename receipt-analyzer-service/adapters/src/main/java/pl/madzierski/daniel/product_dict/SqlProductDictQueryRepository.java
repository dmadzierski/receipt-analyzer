package pl.madzierski.daniel.product_dict;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import pl.madzierski.daniel.product_dict.projection.ProductDictWithAliasesAndCategoryProjection;

import java.util.Set;

public interface SqlProductDictQueryRepository extends ProductDictQueryRepository, Repository<SqlProduct, String> {
    @Query("SELECT d FROM SqlProduct d LEFT JOIN FETCH d.aliases LEFT JOIN FETCH d.productCategory")
    Set<ProductDictWithAliasesAndCategoryProjection> findAllWithCategoryAndAliases();

    long countProductDictEntitiesByProductCategoryId(String productCategoryId);
}
