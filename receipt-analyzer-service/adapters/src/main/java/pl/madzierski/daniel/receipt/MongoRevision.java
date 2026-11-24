package pl.madzierski.daniel.receipt;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import pl.madzierski.daniel.digitization.ReceiptResolverStrategyType;
import pl.madzierski.daniel.receipt.event.RevisionChanged;
import pl.madzierski.daniel.receipt.model.ReceiptItemDto;
import pl.madzierski.daniel.receipt.model.ReceiptRevisionDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Document("revisions")
record MongoRevision(@Id UUID id, long version, @Indexed UUID receiptId, ReceiptResolverStrategyType resolver, LocalDateTime createdDate,
                     BigDecimal totalPrice, LocalDateTime paymentDate, Boolean isCorrect, UUID parentRevisionId,
                     UUID sourceFileGroupId, List<Item> items) {

    record Item(UUID id, String name, @Indexed UUID productId, BigDecimal amount, BigDecimal unitPrice, BigDecimal discount,
                BigDecimal totalPrice, Integer position, UUID parentItemId) {
    }

    static MongoRevision fromEvent(RevisionChanged event) {
        return new MongoRevision(event.id(), event.version(), event.receiptId(), event.resolver(), event.createdDate(), event.totalPrice(),
            event.paymentDate(), event.isCorrect(), event.parentRevisionId(), event.sourceFileGroupId(),
            event.items().stream()
                .map(item -> new Item(item.id(), item.name(), item.productId(), item.amount(), item.unitPrice(),
                    item.discount(), item.totalPrice(), item.position(), item.parentItemId()))
                .toList());
    }

    ReceiptRevisionDto toDto(MongoReceipt receipt) {
        return new ReceiptRevisionDto(id, version, createdDate, resolver, totalPrice, paymentDate, id.equals(receipt.preferredRevisionId()),
            isCorrect, receiptId);
    }

    Set<UUID> productIds() {
        if (resolver == ReceiptResolverStrategyType.USER) return Set.of();
        return items.stream().map(Item::productId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    List<ReceiptItemDto> toItemDtos(Map<UUID, String> productNames) {
        return items.stream()
            .map(item -> new ReceiptItemDto(item.id(), item.productId(), displayName(item, productNames), item.amount(),
                item.unitPrice(), item.discount(), item.totalPrice(), item.position()))
            .toList();
    }

    private String displayName(Item item, Map<UUID, String> productNames) {
        if (resolver == ReceiptResolverStrategyType.USER || item.productId() == null) return item.name();
        return productNames.getOrDefault(item.productId(), item.name());
    }
}
