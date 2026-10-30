package pl.madzierski.daniel.store.model;

import java.util.UUID;


public record CreateStoreRequest(
        UUID storeBrandId,
        String brandName,
        String address,
        String city,
        String postalCode,
        String country) {
}
