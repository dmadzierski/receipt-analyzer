package pl.madzierski.daniel.product_dict.event;

import pl.madzierski.daniel.event.AggregateDeleted;

import java.util.UUID;

public record ProductDictDeleted(UUID id) implements AggregateDeleted {
}
