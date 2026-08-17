package pl.madzierski.daniel.receipt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.madzierski.daniel.product_dict.model.ProductDictQuery;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
class ReceiptItem {

    private final Set<ReceiptItem> childItems = new HashSet<>();
    private String id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private ReceiptRevision receiptRevision;
    private String name;
    private ProductDictQuery nameDict;
    private BigDecimal amount;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal totalPrice;
    private Integer position;
    private ReceiptItem parentItem;

    public ReceiptItem(String id) {
        this.id = id;
    }

    public ReceiptItem(ReceiptRevision receiptRevision, String name, ProductDictQuery nameDict, BigDecimal amount,
                       BigDecimal unitPrice, BigDecimal discount, BigDecimal totalPrice, Integer position, ReceiptItem parentItem) {
        this.receiptRevision = receiptRevision;
        this.name = name;
        this.nameDict = nameDict;
        this.amount = amount;
        this.unitPrice = unitPrice;
        this.discount = discount;
        this.totalPrice = totalPrice;
        this.position = position;
        this.parentItem = parentItem;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ReceiptItem that = (ReceiptItem) o;
        return Objects.equals(name, that.name) && Objects.equals(amount, that.amount) && Objects.equals(unitPrice, that.unitPrice) && Objects.equals(discount, that.discount) && Objects.equals(totalPrice, that.totalPrice) && Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, amount, unitPrice, discount, totalPrice, position);
    }

}