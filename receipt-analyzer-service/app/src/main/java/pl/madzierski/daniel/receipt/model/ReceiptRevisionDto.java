package pl.madzierski.daniel.receipt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;

@AllArgsConstructor
@Getter
@Builder
public class ReceiptRevisionDto {
    private String id;
    private LocalDateTime createdDate;
    private String name;
    private ReceiptResolverStrategyType resolver;
    private String brand;
    private BigDecimal totalPrice;
    private LocalDateTime payingDate;
    private String address;
    private Boolean isPreferredRevision;
    private Boolean isCorrect;
    private Collection<ReceiptItemDto> items;
    private String receiptId;
    private String revision;
}
