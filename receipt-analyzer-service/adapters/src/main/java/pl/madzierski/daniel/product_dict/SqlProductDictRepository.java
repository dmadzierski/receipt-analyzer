package pl.madzierski.daniel.product_dict;


import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.event.EventPublisher;
import pl.madzierski.daniel.product_dict.event.ProductDictDeleted;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SqlProductDictRepository extends JpaRepository<SqlProductDict, UUID> {

    @Query("SELECT DISTINCT p FROM SqlProductDict p LEFT JOIN FETCH p.aliases LEFT JOIN FETCH p.categories")
    List<SqlProductDict> findAllWithAliasesAndCategories();

    long countByIdIn(Collection<UUID> ids);

    @Query("SELECT COUNT(p) > 0 FROM SqlProductDict p JOIN p.categories c WHERE c.id = :productCategoryId")
    boolean isCategoryUsed(UUID productCategoryId);
}

@Repository
@AllArgsConstructor
class ProductDictRepositoryImpl implements ProductDictRepository {

    private final SqlProductDictRepository repository;
    private final EventPublisher eventPublisher;

    @Override
    public Optional<ProductDict> findById(UUID productDictId) {
        return repository.findById(productDictId).map(SqlProductDict::toProductDict);
    }

    @Override
    public List<ProductDict> findAll() {
        return repository.findAllWithAliasesAndCategories().stream().map(SqlProductDict::toProductDict).toList();
    }

    @Override
    public long countByIdIn(Collection<UUID> productDictIds) {
        return repository.countByIdIn(productDictIds);
    }

    @Override
    public boolean isCategoryUsed(UUID productCategoryId) {
        return repository.isCategoryUsed(productCategoryId);
    }

    @Override
    public void deleteAllByIdIn(List<UUID> productDictIdList) {
        repository.deleteAllById(productDictIdList);
        repository.flush();
        productDictIdList.forEach(id -> eventPublisher.publish(new ProductDictDeleted(id)));
    }

    @Override
    public ProductDict save(ProductDict productDict) {
        SqlProductDict saved = repository.saveAndFlush(SqlProductDict.fromProductDict(productDict));
        eventPublisher.publishLatest(saved.getId(), () -> ProductDictSnapshots.changed(saved.toProductDict()));
        return saved.toProductDict();
    }
}
