package pl.madzierski.daniel.store.model;

import java.util.UUID;


public record StoreDetailsResponse (
    UUID id,
    String brand,
    String address,
    String city,
    String postalCode,
    String country
){

    public static StoreDetailsResponse storeMapper(StoreDto storeDto) {
        return new StoreDetailsResponse(
            storeDto.getId(),
            storeDto.getBrand(),
            storeDto.getAddress(),
            storeDto.getCity(),
            storeDto.getPostalCode(),
            storeDto.getCountry()
        );
    }
}
