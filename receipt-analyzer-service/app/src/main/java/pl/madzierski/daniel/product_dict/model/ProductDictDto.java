package pl.madzierski.daniel.product_dict.model;

import java.util.List;
import java.util.UUID;

public record ProductDictDto(UUID id, long version, String name, List<Category> categories, List<Alias> aliases) {

    public record Category(UUID id, String name) {
    }

    public record Alias(UUID id, String name) {
    }
}
