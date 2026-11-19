package pl.madzierski.daniel.receipt.event;

import pl.madzierski.daniel.event.IntegrationEvent;

import java.util.UUID;

public record ReceiptDeleted(UUID id) implements IntegrationEvent {
}
