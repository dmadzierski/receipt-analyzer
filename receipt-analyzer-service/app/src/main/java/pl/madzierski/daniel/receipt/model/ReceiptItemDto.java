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
}
