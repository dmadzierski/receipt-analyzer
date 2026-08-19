package pl.madzierski.daniel.store;

import pl.madzierski.daniel.store.model.StoreDto;

import java.util.Optional;
import java.util.Set;

public interface StoreQueryRepository {
    Set<StoreDto> findStoresByQuery(String query);

    Optional<StoreDto> findStoreById(String storeId);
}
