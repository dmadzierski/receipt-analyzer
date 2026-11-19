package pl.madzierski.daniel.store;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.event.EventPublisher;
import pl.madzierski.daniel.store.event.StoreChanged;

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
        SqlStore entity = this.repository.save(SqlStore.fromStore(store));
        eventPublisher.publishLatest(entity.getId(), () -> {
            Store saved = entity.toStore();
            return new StoreChanged(saved.getId(), saved.getBrand() != null ? saved.getBrand().id() : null,
            saved.getAddress(), saved.getCity(), saved.getPostalCode(), saved.getCountry());
        });
        return entity.toStore();
    }
}
