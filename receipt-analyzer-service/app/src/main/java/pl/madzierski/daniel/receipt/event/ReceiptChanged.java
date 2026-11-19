package pl.madzierski.daniel.receipt.event;

import pl.madzierski.daniel.event.IntegrationEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReceiptChanged(UUID id, UUID ownerId, UUID walletId, UUID storeId, String name, String description,
                             LocalDateTime createdDate, LocalDateTime modifiedDate,
                             UUID preferredRevisionId) implements IntegrationEvent {
}
