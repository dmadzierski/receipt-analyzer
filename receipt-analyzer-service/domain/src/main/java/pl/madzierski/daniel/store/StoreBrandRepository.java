package pl.madzierski.daniel.store;

import pl.madzierski.daniel.store.model.StoreBrand;

import java.util.Optional;
import java.util.UUID;

public interface StoreBrandRepository {

    StoreBrand save(StoreBrand storeBrand);

    Optional<StoreBrand> findById(UUID id);
}
