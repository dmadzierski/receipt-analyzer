package pl.madzierski.daniel.store.model;

import java.util.Set;
import java.util.UUID;

public record StoreListResponse(
    Set<StoreListItem> items
) {

    public static StoreListItem storeMapper(StoreDto storeDto) {
        return new StoreListItem(
            storeDto.getId(),
            storeDto.getBrand(),
            storeDto.getAddress(),
            storeDto.getCity(),
            storeDto.getPostalCode(),
            storeDto.getCountry()
        );
    }

    public record StoreListItem(
        UUID id,
        String brand,
        String address,
        String city,
        String postalCode,
        String country
    ){}
}
