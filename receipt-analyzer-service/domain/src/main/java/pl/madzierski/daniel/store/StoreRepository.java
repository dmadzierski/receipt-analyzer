package pl.madzierski.daniel.store;

import java.util.UUID;

interface StoreRepository {

    Store save(Store store);

    boolean exists(UUID storeId);
}
