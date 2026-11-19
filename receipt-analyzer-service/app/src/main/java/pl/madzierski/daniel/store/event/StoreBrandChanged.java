package pl.madzierski.daniel.store.event;

import pl.madzierski.daniel.event.IntegrationEvent;

import java.util.UUID;

public record StoreBrandChanged(UUID id, String name) implements IntegrationEvent {
}
