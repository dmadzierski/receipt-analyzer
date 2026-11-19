package pl.madzierski.daniel.receipt.event;

import pl.madzierski.daniel.event.AggregateChanged;
import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RevisionChanged(UUID id, long version, UUID receiptId, ReceiptResolverStrategyType resolver, LocalDateTime createdDate,
                              BigDecimal totalPrice, LocalDateTime paymentDate, Boolean isCorrect,
                              UUID parentRevisionId, UUID sourceFileGroupId, List<Item> items) implements AggregateChanged {

    public record Item(UUID id, String name, UUID productId, BigDecimal amount, BigDecimal unitPrice, BigDecimal discount,
                       BigDecimal totalPrice, Integer position, UUID parentItemId) {
    }
}
