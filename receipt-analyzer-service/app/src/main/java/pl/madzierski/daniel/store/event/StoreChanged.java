package pl.madzierski.daniel.store.event;

import pl.madzierski.daniel.event.AggregateChanged;

import java.util.UUID;

public record StoreChanged(UUID id, long version, UUID brandId, String address, String city, String postalCode,
                           String country) implements AggregateChanged {
}
