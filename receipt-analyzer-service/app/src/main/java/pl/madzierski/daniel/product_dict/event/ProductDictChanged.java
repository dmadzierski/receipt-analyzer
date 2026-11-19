package pl.madzierski.daniel.product_dict.event;

import pl.madzierski.daniel.event.AggregateChanged;

import java.util.List;
import java.util.UUID;

public record ProductDictChanged(UUID id, long version, String name, List<Alias> aliases, List<UUID> categoryIds) implements AggregateChanged {

    public record Alias(UUID id, String name) {
    }
}
