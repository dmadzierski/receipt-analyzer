package pl.madzierski.daniel.store.event;

import pl.madzierski.daniel.event.IntegrationEvent;

import java.util.UUID;

public record StoreChanged(UUID id, UUID brandId, String address, String city, String postalCode,
                           String country) implements IntegrationEvent {
}
