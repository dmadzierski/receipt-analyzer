package pl.madzierski.daniel.receipt;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.common.entity.BaseEntity;
import pl.madzierski.daniel.product_dict.model.ProductDictQuery;
import pl.madzierski.daniel.receipt.model.ReceiptItemQuery;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "receipt_item")
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = true, exclude = "parentItem")
class SqlReceiptItem extends BaseEntity {

    private String name;
    @Column(name = "product_id")
    private UUID productId;
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
        sqlReceiptItem.setCreatedDate(item.getCreatedDate());
        sqlReceiptItem.setModifiedDate(item.getModifiedDate());
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
            sqlReceiptItem.setProductId(item.getNameDict().id());
        }
        return sqlReceiptItem;
    }

    public ReceiptItem toReceiptItem() {
        return ReceiptItem.restore(id, createdDate, modifiedDate, name,
            productId != null ? new ProductDictQuery(productId) : null, amount, unitPrice, discount, totalPrice, position,
            parentItem != null ? new ReceiptItemQuery(parentItem.getId()) : null);
    }
}
