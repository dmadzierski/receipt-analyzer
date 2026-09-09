package pl.madzierski.daniel.product_dict;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface SqlProductCategoryRepository extends JpaRepository<SqlProductCategory, String> {
    List<SqlProductCategory> findAllByOrderByNameAsc();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, String id);
}

@org.springframework.stereotype.Repository
@AllArgsConstructor
class ProductCategoryRepositoryImpl implements ProductCategoryRepository {

    private final SqlProductCategoryRepository repository;

    @Override
    public List<ProductCategory> findAllByOrderByNameAsc() {
        return this.repository.findAllByOrderByNameAsc().stream()
            .map(SqlProductCategory::toProductCategory)
            .toList();
    }

    @Override
    public boolean existsByNameAndUser(String name, String userSub) {
        return this.repository.existsByName(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, String id) {
        return this.repository.existsByNameAndIdNot(name, id);
    }

    @Override
    public ProductCategory save(ProductCategory productCategory) {
        return this.repository.save(SqlProductCategory.formProductCategory(productCategory)).toProductCategory();
    }

    @Override
    public Optional<ProductCategory> findById(String productCategoryId) {
        return this.repository.findById(productCategoryId)
            .map(SqlProductCategory::toProductCategory);
    }

    @Override
    public void delete(ProductCategory productCategory) {
        this.repository.delete(SqlProductCategory.formProductCategory(productCategory));
    }
}