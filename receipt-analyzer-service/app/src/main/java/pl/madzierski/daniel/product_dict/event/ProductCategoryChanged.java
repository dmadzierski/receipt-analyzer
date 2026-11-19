package pl.madzierski.daniel.product_dict.event;

import pl.madzierski.daniel.event.AggregateChanged;

import java.util.UUID;

public record ProductCategoryChanged(UUID id, long version, String name) implements AggregateChanged {
}
