package pl.madzierski.daniel.product_dict.event;

import pl.madzierski.daniel.event.IntegrationEvent;

import java.util.List;
import java.util.UUID;

public record ProductDictChanged(UUID id, String name, List<Alias> aliases, List<UUID> categoryIds) implements IntegrationEvent {

    public record Alias(UUID id, String name) {
    }
}
