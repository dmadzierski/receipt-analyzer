package pl.madzierski.daniel.product_dict.event;

import pl.madzierski.daniel.event.IntegrationEvent;

import java.util.UUID;

public record ProductCategoryChanged(UUID id, String name) implements IntegrationEvent {
}
