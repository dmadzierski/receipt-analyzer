package pl.madzierski.daniel.wallet.event;

import pl.madzierski.daniel.event.IntegrationEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record WalletChanged(UUID id, UUID ownerId, String name, LocalDateTime createdDate) implements IntegrationEvent {
}
