package pl.madzierski.daniel.app.receipt.revision;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.app.common.model.BaseEntity;
import pl.madzierski.daniel.app.receipt.ReceiptEntity;
import pl.madzierski.daniel.app.receipt.revision.item.ItemEntity;
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategyType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "receipt_revision")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptRevisionEntity extends BaseEntity {

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
    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, mappedBy = "receiptRevision")
    private final Set<ItemEntity> items = new HashSet<>();
    @OneToMany(mappedBy = "parentReceiptRevision")
    private final Set<ReceiptRevisionEntity> childReceiptRevisions = new HashSet<>();

    public void addItem(ItemEntity item) {
        this.items.add(item);
    }

    public void addItems(Collection<ItemEntity> items) {
        this.items.addAll(items);
    }
}