package pl.madzierski.daniel.event;

import org.springframework.modulith.events.Externalized;

import java.util.UUID;

@Externalized(KafkaEventConfiguration.NAME)
public record PublishedEvent(UUID id, UUID correlationId, UUID aggregateId, String type, String payload) {
}
