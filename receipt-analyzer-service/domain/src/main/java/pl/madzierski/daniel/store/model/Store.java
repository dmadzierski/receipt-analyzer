package pl.madzierski.daniel.store.model;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Store {

    private String id;
    private StoreBrand brand;
    private String address;
    private String city;
    private String postalCode;
    private String country;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

}
