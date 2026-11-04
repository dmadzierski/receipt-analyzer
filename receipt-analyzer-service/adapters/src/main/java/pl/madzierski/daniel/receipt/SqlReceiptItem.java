package pl.madzierski.daniel.receipt;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.common.entity.BaseEntity;
import pl.madzierski.daniel.product_dict.SqlProductDictQuery;

import java.math.BigDecimal;

@Entity
@Table(name = "receipt_item")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"receiptRevision", "parentItem", "product"})
class SqlReceiptItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_revision_id", insertable = false, updatable = false)
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
        if (item.getNameDict() != null) {
            sqlReceiptItem.setProduct(SqlProductDictQuery.fromProductDict(item.getNameDict()));
        }
        return sqlReceiptItem;
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
        receiptItem.setNameDict(this.product != null ? this.product.toProductDictQuery() : null);
        receiptItem.setParentItem(this.parentItem != null ? this.parentItem.toReceiptItem() : null);
        return receiptItem;
    }
}
