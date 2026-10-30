package pl.madzierski.daniel.receipt.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class ReceiptItemDto {
    private UUID id;
    private UUID productDictId;
    private String name;
    private BigDecimal amount;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal totalPrice;
    private Integer position;
    private ReceiptItemDto parentItem;

    public ReceiptItemDto(UUID id, UUID productDictId, String name, BigDecimal amount, BigDecimal unitPrice, BigDecimal discount, BigDecimal totalPrice, Integer position) {
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
