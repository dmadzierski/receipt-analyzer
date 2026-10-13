package pl.madzierski.daniel.receipt;

import lombok.*;
import pl.madzierski.daniel.product_dict.model.ProductDictQuery;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"childItems", "receiptRevision", "parentItem"})
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

}