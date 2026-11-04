package pl.madzierski.daniel.product_dict;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ProductDictRepository {
    Optional<ProductDict> findById(UUID productDictId);

    List<ProductDict> findAll();

    void deleteAllByIdIn(List<UUID> productDictIdList);

    ProductDict save(ProductDict productDict);
}
