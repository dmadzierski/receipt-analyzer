package pl.madzierski.daniel.receipt.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class ReceiptItemDto {
    private String id;
    private String productDictId;
    private String name;
    private Double amount;
    private Double unitPrice;
    private Double discount;
    private Double totalPrice;
    private Integer position;
    private ReceiptItemDto parentItem;

    public ReceiptItemDto(String id, String productDictId, String name, Double amount, Double unitPrice, Double discount, Double totalPrice, Integer position) {
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
