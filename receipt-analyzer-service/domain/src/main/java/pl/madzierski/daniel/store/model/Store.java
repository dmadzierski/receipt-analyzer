package pl.madzierski.daniel.store.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(exclude = "brand")
public class Store {

    private UUID id;
    private StoreBrand brand;
    private String address;
    private String city;
    private String postalCode;
    private String country;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

}
