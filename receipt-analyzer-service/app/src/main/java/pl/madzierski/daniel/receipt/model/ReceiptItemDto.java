package pl.madzierski.daniel.receipt.model;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class ReceiptItemDto {
    private String id;
    private String productDictId;
    private String name;
    private BigDecimal amount;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal totalPrice;
    private Integer position;
    private ReceiptItemDto parentItem;

    public ReceiptItemDto(String id, String productDictId, String name, BigDecimal amount, BigDecimal unitPrice, BigDecimal discount, BigDecimal totalPrice, Integer position) {
        this.id = id;
        this.productDictId = productDictId;
        this.name = name;
        this.amount = amount;
        this.unitPrice = unitPrice;
        this.discount = discount;
        this.totalPrice = totalPrice;
        this.position = position;
    }
}
