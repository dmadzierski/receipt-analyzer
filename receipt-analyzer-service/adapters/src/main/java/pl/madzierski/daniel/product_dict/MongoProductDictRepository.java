package pl.madzierski.daniel.product_dict;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.product_dict.projection.ProductDictWithAliasesAndCategoryProjection;

import java.util.Set;
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
    public Set<ProductDictWithAliasesAndCategoryProjection> findAllWithCategoryAndAliases() {
        return productDictRepository.findAll().stream()
            .map(MongoProductDict::toProjection)
            .collect(Collectors.toSet());
    }
}
