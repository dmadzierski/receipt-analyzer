package pl.madzierski.daniel.receipt;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ReceiptRevision {
    private String id;
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

    public Set<ReceiptItem> getItems() {
        return Collections.unmodifiableSet(items);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ReceiptRevision that = (ReceiptRevision) o;
        return Objects.equals(name, that.name) && Objects.equals(revision, that.revision) && resolver == that.resolver && Objects.equals(brand, that.brand) && Objects.equals(totalPrice, that.totalPrice) && Objects.equals(paymentDate, that.paymentDate) && Objects.equals(address, that.address) && Objects.equals(isPreferredRevision, that.isPreferredRevision) && Objects.equals(isCorrect, that.isCorrect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, revision, resolver, brand, totalPrice, paymentDate, address, isPreferredRevision, isCorrect);
    }

    public void addItems(Set<ReceiptItem> items) {
        this.items.addAll(items);
    }

    public void addItem(ReceiptItem newItem) {
        this.items.add(newItem);
    }
}