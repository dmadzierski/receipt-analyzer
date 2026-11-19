package pl.madzierski.daniel.file_group.event;

import pl.madzierski.daniel.event.IntegrationEvent;

import java.util.UUID;

public record FileGroupDeleted(UUID id) implements IntegrationEvent {
}
