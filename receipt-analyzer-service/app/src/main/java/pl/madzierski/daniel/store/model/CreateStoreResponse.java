package pl.madzierski.daniel.store.model;

public record CreateStoreResponse(
    String id,
    String brand,
    String address,
    String city,
    String postalCode,
    String country
) {
}
