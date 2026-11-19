package pl.madzierski.daniel.wallet.event;

import pl.madzierski.daniel.event.AggregateChanged;

import java.time.LocalDateTime;
import java.util.UUID;

public record WalletChanged(UUID id, long version, UUID ownerId, String name, LocalDateTime createdDate) implements AggregateChanged {
}
