package pl.madzierski.daniel.store.readmodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import pl.madzierski.daniel.store.event.StoreChanged;
import pl.madzierski.daniel.store.model.StoreDto;

import java.util.UUID;

@Document("stores")
record MongoStore(@Id UUID id, @Indexed UUID brandId, String brand, String address, String city, String postalCode,
                  String country) {

    static MongoStore fromEvent(StoreChanged event, String brand) {
        return new MongoStore(event.id(), event.brandId(), brand, event.address(), event.city(), event.postalCode(),
            event.country());
    }

    StoreDto toDto() {
        return new StoreDto(id, brand, address, city, postalCode, country);
    }
}
