package pl.madzierski.daniel.product_dict;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.madzierski.daniel.event.EventPublisher;
import pl.madzierski.daniel.product_dict.event.ProductCategoryDeleted;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

interface SqlProductCategoryRepository extends JpaRepository<SqlProductCategory, UUID> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}

@org.springframework.stereotype.Repository
@AllArgsConstructor
class ProductCategoryRepositoryImpl implements ProductCategoryRepository {

    private final SqlProductCategoryRepository repository;
    private final EventPublisher eventPublisher;

    @Override
    public boolean existsByName(String name) {
        return this.repository.existsByNameIgnoreCase(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, UUID id) {
        return this.repository.existsByNameIgnoreCaseAndIdNot(name, id);
    }

    @Override
    public ProductCategory save(ProductCategory productCategory) {
        SqlProductCategory saved = this.repository.save(SqlProductCategory.fromProductCategory(productCategory));
        eventPublisher.publishLatest(saved.getId(), () -> ProductDictEventMapper.changed(saved.toProductCategory()));
        return saved.toProductCategory();
    }

    @Override
    public Optional<ProductCategory> findById(UUID productCategoryId) {
        return this.repository.findById(productCategoryId)
            .map(SqlProductCategory::toProductCategory);
    }

    @Override
    public void delete(ProductCategory productCategory) {
        this.repository.delete(SqlProductCategory.fromProductCategory(productCategory));
        eventPublisher.publish(new ProductCategoryDeleted(productCategory.getId()));
    }

    @Override
    public Set<ProductCategory> findAllById(Set<UUID> categoryIdList) {
        return this.repository.findAllById(categoryIdList).stream()
            .map(SqlProductCategory::toProductCategory)
            .collect(Collectors.toSet());
    }
}