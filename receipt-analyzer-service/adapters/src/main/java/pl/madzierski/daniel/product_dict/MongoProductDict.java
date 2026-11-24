package pl.madzierski.daniel.product_dict;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import pl.madzierski.daniel.product_dict.event.ProductDictChanged;
import pl.madzierski.daniel.product_dict.model.ProductDictDto;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Document("product_dicts")
record MongoProductDict(@Id UUID id, long version, String name, List<Alias> aliases, List<Category> categories) {

    record Alias(UUID id, String name) {
    }

    record Category(UUID id, String name) {
    }

    static MongoProductDict fromEvent(ProductDictChanged event, Function<UUID, String> categoryName) {
        return new MongoProductDict(event.id(), event.version(), event.name(),
            event.aliases().stream().map(alias -> new Alias(alias.id(), alias.name())).toList(),
            event.categoryIds().stream().map(categoryId -> new Category(categoryId, categoryName.apply(categoryId))).toList());
    }

    ProductDictDto toDto() {
        return new ProductDictDto(id, version, name,
            categories.stream()
                .filter(category -> category.name() != null)
                .map(category -> new ProductDictDto.Category(category.id(), category.name()))
                .toList(),
            aliases.stream()
                .map(alias -> new ProductDictDto.Alias(alias.id(), alias.name()))
                .toList());
    }
}
