package pl.madzierski.daniel.store;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.madzierski.daniel.store.event.StoreBrandChanged;
import pl.madzierski.daniel.store.event.StoreChanged;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class StoreEventMapper {

    static StoreBrandChanged changed(StoreBrand brand) {
        return new StoreBrandChanged(brand.getId(), brand.getVersion(), brand.getName());
    }

    static StoreChanged changed(Store store) {
        return new StoreChanged(store.getId(), store.getVersion(), store.getBrand() != null ? store.getBrand().id() : null, store.getAddress(),
            store.getCity(), store.getPostalCode(), store.getCountry());
    }
}
