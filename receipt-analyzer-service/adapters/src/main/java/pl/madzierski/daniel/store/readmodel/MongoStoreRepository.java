package pl.madzierski.daniel.store.readmodel;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.store.StoreQueryRepository;
import pl.madzierski.daniel.store.model.StoreDto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

interface MongoStoreRepository extends MongoRepository<MongoStore, UUID> {

    @Query("""
        { '$or': [
            { 'brand': { '$regex': ?0, '$options': 'i' } },
            { 'address': { '$regex': ?0, '$options': 'i' } },
            { 'city': { '$regex': ?0, '$options': 'i' } },
            { 'postalCode': { '$regex': ?0, '$options': 'i' } }
        ] }
        """)
    List<MongoStore> findAllContaining(String pattern);

    @Query("{ 'brandId': ?0 }")
    @Update("{ '$set': { 'brand': ?1 } }")
    void updateBrandName(UUID brandId, String brand);
}

@Repository
@RequiredArgsConstructor
class StoreQueryRepositoryImpl implements StoreQueryRepository {

    private final MongoStoreRepository repository;

    @Override
    public Set<StoreDto> findStoresByQuery(String query) {
        if (query == null) return Set.of();
        return toDtos(repository.findAllContaining(Pattern.quote(query)));
    }

    @Override
    public Optional<StoreDto> findStoreById(UUID storeId) {
        return repository.findById(storeId).map(MongoStore::toDto);
    }

    @Override
    public Set<StoreDto> findStoresByIds(Collection<UUID> storeIds) {
        return toDtos(repository.findAllById(storeIds));
    }

    private static Set<StoreDto> toDtos(List<MongoStore> stores) {
        return stores.stream().map(MongoStore::toDto).collect(Collectors.toSet());
    }
}
