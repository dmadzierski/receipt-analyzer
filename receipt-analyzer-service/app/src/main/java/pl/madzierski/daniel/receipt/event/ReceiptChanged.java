package pl.madzierski.daniel.receipt.event;

import pl.madzierski.daniel.event.AggregateChanged;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReceiptChanged(UUID id, long version, UUID ownerId, UUID walletId, UUID storeId, String name, String description,
                             LocalDateTime createdDate, LocalDateTime modifiedDate,
                             UUID preferredRevisionId) implements AggregateChanged {
}
