package pl.madzierski.daniel.store;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pl.madzierski.daniel.store.model.StoreBrandQuery;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(exclude = "brand")
class Store {

    private UUID id;
    private StoreBrandQuery brand;
    private String address;
    private String city;
    private String postalCode;
    private String country;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    static Store create(StoreBrandQuery brand, String address, String city, String postalCode, String country) {
        return builder()
            .brand(brand)
            .address(address)
            .city(city)
            .postalCode(postalCode)
            .country(country)
            .build();
    }

    static Store restore(UUID id, StoreBrandQuery brand, String address, String city, String postalCode, String country,
                         LocalDateTime createdDate, LocalDateTime modifiedDate) {
        return builder()
            .id(id)
            .brand(brand)
            .address(address)
            .city(city)
            .postalCode(postalCode)
            .country(country)
            .createdDate(createdDate)
            .modifiedDate(modifiedDate)
            .build();
    }
}
