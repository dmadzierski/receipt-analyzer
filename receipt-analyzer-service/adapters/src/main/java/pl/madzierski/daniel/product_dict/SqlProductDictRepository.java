package pl.madzierski.daniel.product_dict;


import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SqlProductDictRepository extends JpaRepository<SqlProductDict, UUID> {
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
    public void deleteAllByIdIn(List<UUID> productDictIdList) {
        repository.deleteAllById(productDictIdList);
    }

    @Override
    public List<ProductDict> saveAll(List<ProductDict> entities) {
        return repository.saveAllAndFlush(entities.stream().map(SqlProductDict::fromProductDict).toList()).stream().map(SqlProductDict::toProductDict).toList();
    }

    @Override
    public void save(ProductDict productDict) {
        repository.saveAndFlush(SqlProductDict.fromProductDict(productDict));
    }
}
