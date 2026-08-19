package pl.madzierski.daniel.store;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.store.model.Store;
import pl.madzierski.daniel.store.model.StoreRepository;

interface SqlStoreRepository extends JpaRepository<SqlStore, String> {
}

@AllArgsConstructor
@Repository
class StoreRepositoryImpl implements StoreRepository {

    private final SqlStoreRepository repository;

    @Override
    public Store save(Store store) {
        return this.repository.save(SqlStore.fromStore(store)).toStore();
    }
}
