package pl.madzierski.daniel.store.readmodel;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.store.StoreBrandQueryRepository;
import pl.madzierski.daniel.store.model.StoreBrandDto;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

interface MongoStoreBrandRepository extends MongoRepository<MongoStoreBrand, UUID> {

    @Query("{ 'name': { '$regex': ?0, '$options': 'i' } }")
    List<MongoStoreBrand> findAllContaining(String pattern);
}

@Repository
@RequiredArgsConstructor
class StoreBrandQueryRepositoryImpl implements StoreBrandQueryRepository {

    private final MongoStoreBrandRepository repository;

    @Override
    public Set<StoreBrandDto> findStoreBrandsByQuery(String query) {
        if (query == null) return Set.of();
        return repository.findAllContaining(Pattern.quote(query)).stream()
            .map(MongoStoreBrand::toDto)
            .collect(Collectors.toSet());
    }
}
