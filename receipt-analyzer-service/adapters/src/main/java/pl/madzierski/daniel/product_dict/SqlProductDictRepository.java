package pl.madzierski.daniel.product_dict;


import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SqlProductDictRepository extends JpaRepository<SqlProductDict, UUID> {

    @Query("SELECT DISTINCT p FROM SqlProductDict p LEFT JOIN FETCH p.aliases LEFT JOIN FETCH p.categories")
    List<SqlProductDict> findAllWithAliasesAndCategories();
}

@Repository
@AllArgsConstructor
class ProductDictRepositoryImpl implements ProductDictRepository {

    private final SqlProductDictRepository repository;

    @Override
    public Optional<ProductDict> findById(UUID productDictId) {
        return repository.findById(productDictId).map(SqlProductDict::toProductDict);
    }

    @Override
    public List<ProductDict> findAll() {
        return repository.findAllWithAliasesAndCategories().stream().map(SqlProductDict::toProductDict).toList();
    }

    @Override
    public void deleteAllByIdIn(List<UUID> productDictIdList) {
        repository.deleteAllById(productDictIdList);
    }

    @Override
    public ProductDict save(ProductDict productDict) {
        return repository.saveAndFlush(SqlProductDict.fromProductDict(productDict)).toProductDict();
    }
}
