package pl.madzierski.daniel.receipt;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.madzierski.daniel.product_dict.SqlProductDictQuery;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "receipt_item")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class})
class SqlReceiptItem {

    @Id
    @UuidGenerator
    private String id;
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    @LastModifiedDate
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;
    @ManyToOne(fetch = FetchType.LAZY)
    private SqlReceiptRevision receiptRevision;
    private String name;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private SqlProductDictQuery product;
    @Column(precision = 10, scale = 3)
    private BigDecimal amount;
    @Column(precision = 10, scale = 2)
    private BigDecimal unitPrice;
    @Column(precision = 10, scale = 2)
    private BigDecimal discount;
    @Column(precision = 10, scale = 2)
    private BigDecimal totalPrice;
    private Integer position;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_item_id")
    private SqlReceiptItem parentItem;
    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "parentItem")
    private final Set<SqlReceiptItem> childItems = new HashSet<>();

    public static SqlReceiptItem fromReceiptItem(ReceiptItem item) {
        SqlReceiptItem sqlReceiptItem = new SqlReceiptItem();
        sqlReceiptItem.setId(item.getId());
        sqlReceiptItem.setName(item.getName());
        sqlReceiptItem.setAmount(item.getAmount());
        sqlReceiptItem.setUnitPrice(item.getUnitPrice());
        sqlReceiptItem.setDiscount(item.getDiscount());
        sqlReceiptItem.setTotalPrice(item.getTotalPrice());
        sqlReceiptItem.setPosition(item.getPosition());
        sqlReceiptItem.setParentItem(item.getParentItem() != null ? SqlReceiptItem.fromReceiptItem(item.getParentItem()) : null);
        return sqlReceiptItem;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SqlReceiptItem that = (SqlReceiptItem) o;
        return Objects.equals(name, that.name) && Objects.equals(amount, that.amount) && Objects.equals(unitPrice, that.unitPrice) && Objects.equals(discount, that.discount) && Objects.equals(totalPrice, that.totalPrice) && Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, amount, unitPrice, discount, totalPrice, position);
    }

    public ReceiptItem toReceiptItem() {
        ReceiptItem receiptItem = new ReceiptItem();
        receiptItem.setId(this.id);
        receiptItem.setName(this.name);
        receiptItem.setAmount(this.amount);
        receiptItem.setUnitPrice(this.unitPrice);
        receiptItem.setDiscount(this.discount);
        receiptItem.setTotalPrice(this.totalPrice);
        receiptItem.setPosition(this.position);
        receiptItem.setParentItem(this.parentItem != null ? this.parentItem.toReceiptItem() : null);
        return receiptItem;
    }
}