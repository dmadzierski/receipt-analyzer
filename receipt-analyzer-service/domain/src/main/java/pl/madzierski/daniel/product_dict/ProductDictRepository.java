package pl.madzierski.daniel.product_dict;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ProductDictRepository {
    Optional<ProductDict> findById(UUID productDictId);

    void deleteAllByIdIn(List<UUID> productDictIdList);

    List<ProductDict> saveAll(List<ProductDict> entities);

    void save(ProductDict productDict);
}
