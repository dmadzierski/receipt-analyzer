package pl.madzierski.daniel.event;

import java.util.UUID;
import java.util.function.Supplier;

public interface EventPublisher {
    void publish(IntegrationEvent event);

    void publishLatest(UUID aggregateId, Supplier<IntegrationEvent> snapshot);
}
