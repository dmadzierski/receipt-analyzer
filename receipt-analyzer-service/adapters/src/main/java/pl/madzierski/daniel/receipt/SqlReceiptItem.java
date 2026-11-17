package pl.madzierski.daniel.receipt;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.common.entity.BaseEntity;
import pl.madzierski.daniel.product_dict.SqlProductDictQuery;
import pl.madzierski.daniel.receipt.model.ReceiptItemQuery;

import java.math.BigDecimal;

@Entity
@Table(name = "receipt_item")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"parentItem", "product"})
class SqlReceiptItem extends BaseEntity {

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
        if (item.getParentItem() != null) {
            SqlReceiptItem parent = new SqlReceiptItem();
            parent.setId(item.getParentItem().id());
            sqlReceiptItem.setParentItem(parent);
        }
        if (item.getNameDict() != null) {
            sqlReceiptItem.setProduct(SqlProductDictQuery.fromProductDict(item.getNameDict()));
        }
        return sqlReceiptItem;
    }

    public ReceiptItem toReceiptItem() {
        return ReceiptItem.restore(id, createdDate, modifiedDate, name,
            product != null ? product.toProductDictQuery() : null, amount, unitPrice, discount, totalPrice, position,
            parentItem != null ? new ReceiptItemQuery(parentItem.getId()) : null);
    }
}
