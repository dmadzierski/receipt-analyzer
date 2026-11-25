package pl.madzierski.daniel.product_dict.readmodel;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.product_dict.ProductDictQueryRepository;
import pl.madzierski.daniel.product_dict.model.ProductDictDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

interface MongoProductDictRepository extends MongoRepository<MongoProductDict, UUID> {

    @Query("{ 'categories.id': ?0 }")
    @Update("{ '$set': { 'categories.$.name': ?1 } }")
    void renameCategory(UUID categoryId, String name);
}

@Repository
@RequiredArgsConstructor
class ProductDictQueryRepositoryImpl implements ProductDictQueryRepository {

    private final MongoProductDictRepository productDictRepository;

    @Override
    public List<ProductDictDto> findAllWithCategoryAndAliases() {
        return productDictRepository.findAll().stream()
            .map(MongoProductDict::toDto)
            .toList();
    }

    @Override
    public Map<UUID, String> findNamesByIds(Collection<UUID> productIds) {
        return productDictRepository.findAllById(productIds).stream()
            .collect(Collectors.toMap(MongoProductDict::id, MongoProductDict::name));
    }
}
