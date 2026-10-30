package pl.madzierski.daniel.store;

import pl.madzierski.daniel.store.model.StoreDto;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface StoreQueryRepository {
    Set<StoreDto> findStoresByQuery(String query);

    Optional<StoreDto> findStoreById(UUID storeId);

    Set<StoreDto> findStoresByIds(Collection<UUID> storeIds);
}
