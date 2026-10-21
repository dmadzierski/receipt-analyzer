package pl.madzierski.daniel.product_dict;

import java.util.List;
import java.util.Optional;
import java.util.Set;

interface ProductCategoryRepository {
    List<ProductCategory> findAllByOrderByNameAsc();

    boolean existsByNameAndUser(String name, String userSub);

    boolean existsByNameAndIdNot(String name, String id);

    ProductCategory save(ProductCategory productCategory);

    Optional<ProductCategory> findById(String productCategoryId);

    void delete(ProductCategory productCategory);

    Set<ProductCategory> findAllById(Set<String> categoryIdList);
}
