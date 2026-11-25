package pl.madzierski.daniel.store;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.event.EventPublisher;

import java.util.UUID;

interface SqlStoreRepository extends JpaRepository<SqlStore, UUID> {
}

@AllArgsConstructor
@Repository
class StoreRepositoryImpl implements StoreRepository {

    private final SqlStoreRepository repository;
    private final EventPublisher eventPublisher;

    @Override
    public Store save(Store store) {
        SqlStore saved = this.repository.save(SqlStore.fromStore(store));
        eventPublisher.publishLatest(saved.getId(), () -> StoreEventMapper.changed(saved.toStore()));
        return saved.toStore();
    }
}
