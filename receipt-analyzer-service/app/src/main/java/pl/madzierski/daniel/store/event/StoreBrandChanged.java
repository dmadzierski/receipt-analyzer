package pl.madzierski.daniel.store.event;

import pl.madzierski.daniel.event.AggregateChanged;

import java.util.UUID;

public record StoreBrandChanged(UUID id, long version, String name) implements AggregateChanged {
}
