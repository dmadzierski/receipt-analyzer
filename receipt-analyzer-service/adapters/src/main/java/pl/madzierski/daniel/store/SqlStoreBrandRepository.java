package pl.madzierski.daniel.store;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.event.EventPublisher;

import java.util.Optional;
import java.util.UUID;

interface SqlStoreBrandRepository extends JpaRepository<SqlStoreBrand, UUID> {
}

@AllArgsConstructor
@Repository
class StoreBrandRepositoryImpl implements StoreBrandRepository {

    private final SqlStoreBrandRepository repository;
    private final EventPublisher eventPublisher;

    @Override
    public StoreBrand save(StoreBrand storeBrand) {
        SqlStoreBrand saved = this.repository.save(SqlStoreBrand.fromStoreBrand(storeBrand));
        eventPublisher.publishLatest(saved.getId(), () -> StoreSnapshots.changed(saved.toStoreBrand()));
        return saved.toStoreBrand();
    }

    @Override
    public Optional<StoreBrand> findById(UUID id) {
        return this.repository.findById(id).map(SqlStoreBrand::toStoreBrand);
    }
}
