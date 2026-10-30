package pl.madzierski.daniel.store.model;

import java.util.UUID;

public record CreateStoreResponse(
    UUID id,
    String brand,
    String address,
    String city,
    String postalCode,
    String country
) {
}
