package pl.madzierski.daniel.receipt.event;

import pl.madzierski.daniel.event.IntegrationEvent;

import java.util.List;
import java.util.UUID;

public record ItemProductsReassigned(UUID id, List<UUID> fromProductIds) implements IntegrationEvent {
}
