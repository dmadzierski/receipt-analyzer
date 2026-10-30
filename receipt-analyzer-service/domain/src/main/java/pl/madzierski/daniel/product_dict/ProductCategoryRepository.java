package pl.madzierski.daniel.product_dict;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

interface ProductCategoryRepository {
    List<ProductCategory> findAllByOrderByNameAsc();

    boolean existsByNameAndUser(String name, String userSub);

    boolean existsByNameAndIdNot(String name, UUID id);

    ProductCategory save(ProductCategory productCategory);

    Optional<ProductCategory> findById(UUID productCategoryId);

    void delete(ProductCategory productCategory);

    Set<ProductCategory> findAllById(Set<UUID> categoryIdList);
}
