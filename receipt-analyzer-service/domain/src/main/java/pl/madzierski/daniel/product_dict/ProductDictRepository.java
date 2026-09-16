package pl.madzierski.daniel.product_dict;

import java.util.List;
import java.util.Optional;

interface ProductDictRepository {
    Optional<ProductDict> findById(String productDictId);

    void deleteAllByIdIn(List<String> productDictIdList);

    List<ProductDict> saveAll(List<ProductDict> entities);

    void save(ProductDict productDict);
}
