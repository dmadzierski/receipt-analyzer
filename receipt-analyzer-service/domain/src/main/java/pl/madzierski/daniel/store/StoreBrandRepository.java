package pl.madzierski.daniel.store;

import java.util.Optional;
import java.util.UUID;

interface StoreBrandRepository {

    StoreBrand save(StoreBrand storeBrand);

    Optional<StoreBrand> findById(UUID id);
}
