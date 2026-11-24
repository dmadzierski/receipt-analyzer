package pl.madzierski.daniel.store.model;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record StoreListResponse(
    Set<StoreListItem> items
) {

    public StoreListResponse(Collection<StoreDto> stores) {
        this(stores.stream().map(StoreListItem::new).collect(Collectors.toSet()));
    }

    public record StoreListItem(
        UUID id,
        String brand,
        String address,
        String city,
        String postalCode,
        String country
    ) {
        StoreListItem(StoreDto store) {
            this(
                store.getId(),
                store.getBrand(),
                store.getAddress(),
                store.getCity(),
                store.getPostalCode(),
                store.getCountry()
            );
        }
    }
}
