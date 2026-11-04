package pl.madzierski.daniel.receipt;

import lombok.*;
import pl.madzierski.daniel.product_dict.model.ProductDictQuery;
import pl.madzierski.daniel.receipt.model.ReceiptItemQuery;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
class ReceiptItem {

    private UUID id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String name;
    private ProductDictQuery nameDict;
    private BigDecimal amount;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal totalPrice;
    private Integer position;
    private ReceiptItemQuery parentItem;

    static ReceiptItem create(String name, ProductDictQuery nameDict, BigDecimal amount, BigDecimal unitPrice,
                              BigDecimal discount, BigDecimal totalPrice, Integer position) {
        return builder()
            .name(name)
            .nameDict(nameDict)
            .amount(amount)
            .unitPrice(unitPrice)
            .discount(discount)
            .totalPrice(totalPrice)
            .position(position)
            .build();
    }

    static ReceiptItem create(ReceiptItemChange change) {
        return create(change.name(), change.nameDict(), change.amount(), change.unitPrice(), change.discount(),
            change.totalPrice(), change.position());
    }

    static ReceiptItem restore(UUID id, LocalDateTime createdDate, LocalDateTime modifiedDate, String name,
                               ProductDictQuery nameDict, BigDecimal amount, BigDecimal unitPrice, BigDecimal discount,
                               BigDecimal totalPrice, Integer position, ReceiptItemQuery parentItem) {
        return builder()
            .id(id)
            .createdDate(createdDate)
            .modifiedDate(modifiedDate)
            .name(name)
            .nameDict(nameDict)
            .amount(amount)
            .unitPrice(unitPrice)
            .discount(discount)
            .totalPrice(totalPrice)
            .position(position)
            .parentItem(parentItem)
            .build();
    }

    ReceiptItem copy() {
        return builder()
            .name(name)
            .nameDict(nameDict)
            .amount(amount)
            .unitPrice(unitPrice)
            .discount(discount)
            .totalPrice(totalPrice)
            .position(position)
            .parentItem(new ReceiptItemQuery(id))
            .build();
    }

    void apply(ReceiptItemChange change) {
        this.name = change.name();
        this.nameDict = change.nameDict();
        this.amount = change.amount();
        this.unitPrice = change.unitPrice();
        this.discount = change.discount();
        this.totalPrice = change.totalPrice();
        this.position = change.position();
    }

    void assignProduct(ProductDictQuery nameDict) {
        this.nameDict = nameDict;
    }

    void rename(String name) {
        this.name = name;
    }

    boolean isPriceConsistent() {
        if (amount == null || unitPrice == null || totalPrice == null) return false;
        BigDecimal itemDiscount = discount != null ? discount : BigDecimal.ZERO;
        BigDecimal expectedTotal = amount.multiply(unitPrice)
            .setScale(2, RoundingMode.HALF_UP)
            .subtract(itemDiscount);
        return expectedTotal.compareTo(totalPrice) == 0;
    }
}
