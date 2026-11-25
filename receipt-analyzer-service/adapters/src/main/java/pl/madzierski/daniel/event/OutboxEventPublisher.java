package pl.madzierski.daniel.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;
    private final List<CommittedEventsListener> committedEventsListeners;

    @Override
    public void publish(IntegrationEvent event) {
        pendingEvents().add(event);
    }

    @Override
    public void publishLatest(UUID aggregateId, Supplier<IntegrationEvent> snapshot) {
        pendingEvents().addLatest(aggregateId, snapshot);
    }

    public void publishPending() {
        Optional<PendingEvents> pending = PendingEvents.find();
        if(pending.isEmpty()) return;
        entityManager.flush();
        TransactionCorrelation correlation = TransactionCorrelation.current(
            committed -> committedEventsListeners.forEach(listener -> listener.onCommit(committed.getId(), committed.getEventCount())));
        pending.get().drain().forEach(event -> {
            applicationEventPublisher.publishEvent(new PublishedEvent(UUID.randomUUID(), correlation.getId(), event.id(),
                EventTypes.typeOf(event), toJson(event)));
            correlation.increaseCountEvent();
        });
    }

    private PendingEvents pendingEvents() {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new AppRuntimeException(AppRuntimeExceptionMessages.EVENT_OUTSIDE_TRANSACTION);
        return PendingEvents.current(this::publishPending);
    }

    private String toJson(IntegrationEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.EVENT_NOT_SERIALIZABLE, e);
        }
    }
}
