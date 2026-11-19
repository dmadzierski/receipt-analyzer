package pl.madzierski.daniel.event;

import lombok.NoArgsConstructor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;
import java.util.function.Consumer;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
final class TransactionCorrelation {

    private static final Object RESOURCE_KEY = TransactionCorrelation.class;

    private final UUID id = UUID.randomUUID();
    private int eventCount;

    static TransactionCorrelation current(Consumer<TransactionCorrelation> onCommit) {
        TransactionCorrelation existing = (TransactionCorrelation) TransactionSynchronizationManager.getResource(RESOURCE_KEY);
        if (existing != null) return existing;
        TransactionCorrelation created = new TransactionCorrelation();
        TransactionSynchronizationManager.bindResource(RESOURCE_KEY, created);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                onCommit.accept(created);
            }

            @Override
            public void afterCompletion(int status) {
                TransactionSynchronizationManager.unbindResourceIfPossible(RESOURCE_KEY);
            }
        });
        return created;
    }

    UUID getId() {
        return id;
    }

    int getEventCount() {
        return eventCount;
    }

    void increaseCountEvent() {
        eventCount++;
    }
}
