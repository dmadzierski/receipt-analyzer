package pl.madzierski.daniel.app.receipt.revision;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.app.common.model.BaseEntity;
import pl.madzierski.daniel.app.receipt.ReceiptEntity;
import pl.madzierski.daniel.app.receipt.revision.item.ItemEntity;
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategyType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "receipt_revision")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptRevisionEntity extends BaseEntity {

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, mappedBy = "receiptRevision")
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final Set<ItemEntity> items = new HashSet<>();
    @OneToMany(mappedBy = "parentReceiptRevision")
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final Set<ReceiptRevisionEntity> childReceiptRevisions = new HashSet<>();
    private String name;
    private String revision;
    private ReceiptResolverStrategyType resolver;
    private String brand;
    private Double totalPrice;
    private String payingDate;
    private String address;
    private Boolean isPreferredRevision;
    private Boolean isCorrect;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id")
    private ReceiptEntity receipt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_receipt_revision_id")
    private ReceiptRevisionEntity parentReceiptRevision;

    public void addItem(ItemEntity item) {
        this.items.add(item);
    }

    public void addItems(Collection<ItemEntity> items) {
        this.items.addAll(items);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ReceiptRevisionEntity that = (ReceiptRevisionEntity) o;
        return Objects.equals(name, that.name) && Objects.equals(revision, that.revision) && resolver == that.resolver && Objects.equals(brand, that.brand) && Objects.equals(totalPrice, that.totalPrice) && Objects.equals(payingDate, that.payingDate) && Objects.equals(address, that.address) && Objects.equals(isPreferredRevision, that.isPreferredRevision) && Objects.equals(isCorrect, that.isCorrect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, revision, resolver, brand, totalPrice, payingDate, address, isPreferredRevision, isCorrect);
    }

    public Set<ItemEntity> getItems() {
        return items.stream().collect(Collectors.toUnmodifiableSet());
    }

    public Boolean removeItem(String itemId) {
        return items.removeIf(itemEntity -> itemEntity.getId().equals(itemId));
    }

    public Set<ReceiptRevisionEntity> getChildReceiptRevisions() {
        return childReceiptRevisions.stream().collect(Collectors.toUnmodifiableSet());
    }
}