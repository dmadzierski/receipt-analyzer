package pl.madzierski.daniel.store.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StoreDto {

    private String id;
    private String brand;
    private String address;
    private String city;
    private String postalCode;
    private String country;

}
