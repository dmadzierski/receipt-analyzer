package pl.madzierski.daniel.product_dict.readmodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import pl.madzierski.daniel.product_dict.event.ProductCategoryChanged;
import pl.madzierski.daniel.product_dict.model.ProductCategoryDto;

import java.util.UUID;

@Document("product_categories")
record MongoProductCategory(@Id UUID id, long version, String name) {

    static MongoProductCategory fromEvent(ProductCategoryChanged event) {
        return new MongoProductCategory(event.id(), event.version(), event.name());
    }

    ProductCategoryDto toDto() {
        return new ProductCategoryDto(id, version, name);
    }
}
