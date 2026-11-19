package pl.madzierski.daniel.event;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class PendingEvents {

    private static final Object RESOURCE_KEY = PendingEvents.class;

    private final List<Supplier<IntegrationEvent>> events = new ArrayList<>();
    private final Map<UUID, Integer> snapshotPositions = new HashMap<>();

    static PendingEvents current(Runnable beforeCommit) {
        return find().orElseGet(() -> createForCurrentTransaction(beforeCommit));
    }

    static Optional<PendingEvents> find() {
        return Optional.ofNullable((PendingEvents) TransactionSynchronizationManager.getResource(RESOURCE_KEY));
    }

    private static PendingEvents createForCurrentTransaction(Runnable beforeCommit) {
        PendingEvents created = new PendingEvents();
        TransactionSynchronizationManager.bindResource(RESOURCE_KEY, created);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void beforeCommit(boolean readOnly) {
                beforeCommit.run();
            }

            @Override
            public void afterCompletion(int status) {
                TransactionSynchronizationManager.unbindResourceIfPossible(RESOURCE_KEY);
            }
        });
        return created;
    }

    void add(IntegrationEvent event) {
        events.add(() -> event);
    }

    void addLatest(UUID aggregateId, Supplier<IntegrationEvent> snapshot) {
        Integer position = snapshotPositions.get(aggregateId);
        if (position != null) {
            events.set(position, snapshot);
            return;
        }
        snapshotPositions.put(aggregateId, events.size());
        events.add(snapshot);
    }

    List<IntegrationEvent> drain() {
        List<IntegrationEvent> drained = events.stream().map(Supplier::get).toList();
        events.clear();
        snapshotPositions.clear();
        return drained;
    }
}
