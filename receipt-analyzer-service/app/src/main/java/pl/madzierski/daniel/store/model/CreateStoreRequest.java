package pl.madzierski.daniel.store.model;


public record CreateStoreRequest(
        String storeBrandId,
        String brandName,
        String address,
        String city,
        String postalCode,
        String country) {
}
