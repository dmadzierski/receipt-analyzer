package pl.madzierski.daniel.app.receipt.revision.item;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.app.common.model.BaseEntity;
import pl.madzierski.daniel.app.product_dict.ProductDictEntity;
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionEntity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "receipt_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptItemEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private ReceiptRevisionEntity receiptRevision;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_dict_id")
    private ProductDictEntity nameDict;

    private Double amount;

    private Double unitPrice;

    private Double discount;

    private Double totalPrice;

    private Integer position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_item_id")
    private ReceiptItemEntity parentItem;

    @OneToMany(mappedBy = "parentItem", fetch = FetchType.LAZY)
    private Set<ReceiptItemEntity> childItems = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ReceiptItemEntity that = (ReceiptItemEntity) o;
        return Objects.equals(name, that.name) && Objects.equals(amount, that.amount) && Objects.equals(unitPrice, that.unitPrice) && Objects.equals(discount, that.discount) && Objects.equals(totalPrice, that.totalPrice) && Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, amount, unitPrice, discount, totalPrice, position);
    }
}