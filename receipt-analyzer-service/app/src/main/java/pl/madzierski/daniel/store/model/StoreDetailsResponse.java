package pl.madzierski.daniel.store.model;


public record StoreDetailsResponse (
    String id,
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
