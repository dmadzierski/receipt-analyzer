package pl.madzierski.daniel.product_dict;


import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

interface SqlProductDictRepository extends JpaRepository<SqlProduct, String> {
}

@Repository
@AllArgsConstructor
class ProductDictRepositoryImpl implements ProductDictRepository {

    private final SqlProductDictRepository repository;

    @Override
    public Optional<ProductDict> findById(String productDictId) {
        return repository.findById(productDictId).map(SqlProduct::toProductDict);
    }

    @Override
    public void deleteAllByIdIn(List<String> productDictIdList) {
        repository.deleteAllById(productDictIdList);
    }

    @Override
    public <S extends ProductDict> List<S> saveAll(Iterable<S> entities) {
        return (List<S>) repository.saveAll(((List<S>) entities).stream().map(SqlProduct::fromProductDict).toList()).stream().map(SqlProduct::toProductDict)
            .toList();
    }
}
