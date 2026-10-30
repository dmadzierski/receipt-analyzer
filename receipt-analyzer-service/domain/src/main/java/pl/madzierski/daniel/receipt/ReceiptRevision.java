package pl.madzierski.daniel.receipt;

import lombok.*;
import pl.madzierski.daniel.file_group.model.FileGroupQuery;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"items", "receipt", "parentReceiptRevision", "childReceiptRevisions", "sourceFileGroup"})
class ReceiptRevision {
    private UUID id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String name;
    private String revision;
    private ReceiptResolverStrategyType resolver;
    private String brand;
    private BigDecimal totalPrice;
    private LocalDateTime paymentDate;
    private String address;
    private Boolean isPreferredRevision;
    private Boolean isCorrect;
    private Receipt receipt;
    private ReceiptRevision parentReceiptRevision;
    private Set<ReceiptItem> items = new HashSet<>();
    private Set<ReceiptRevision> childReceiptRevisions = new HashSet<>();
    private FileGroupQuery sourceFileGroup;

    public Set<ReceiptItem> getItems() {
        return Collections.unmodifiableSet(items);
    }

    public void addItems(Set<ReceiptItem> items) {
        this.items.addAll(items);
    }

    public void addItem(ReceiptItem newItem) {
        this.items.add(newItem);
    }

    public void removeItem(UUID id) {
        this.items.removeIf(item -> item.getId().equals(id));
    }
}