package pl.madzierski.daniel.store;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import pl.madzierski.daniel.store.event.StoreBrandChanged;
import pl.madzierski.daniel.store.model.StoreBrandDto;

import java.util.UUID;

@Document("store_brands")
record MongoStoreBrand(@Id UUID id, String name) {

    static MongoStoreBrand fromEvent(StoreBrandChanged event) {
        return new MongoStoreBrand(event.id(), event.name());
    }

    StoreBrandDto toDto() {
        return new StoreBrandDto(id, name);
    }
}
