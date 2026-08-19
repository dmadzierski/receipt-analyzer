package pl.madzierski.daniel.store;

import pl.madzierski.daniel.store.model.StoreBrandDto;

import java.util.Set;

public interface StoreBrandQueryRepository {

    Set<StoreBrandDto> findStoreBrandsByQuery(String query);
}
