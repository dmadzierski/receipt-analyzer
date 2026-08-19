package pl.madzierski.daniel.store;

import pl.madzierski.daniel.store.model.StoreBrand;

import java.util.Optional;

public interface StoreBrandRepository {

    StoreBrand save(StoreBrand storeBrand);

    Optional<StoreBrand> findById(String id);
}
