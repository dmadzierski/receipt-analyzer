package pl.madzierski.daniel.app.product_dict.product_category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface ProductCategoryRepository extends JpaRepository<ProductCategoryEntity, String> {
    List<ProductCategoryEntity> findAllByOrderByNameAsc();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, String id);
}
