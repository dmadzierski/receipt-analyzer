package pl.madzierski.daniel.receipt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Builder
public class ReceiptRevisionDto {
    private UUID id;
    private LocalDateTime createdDate;
    private String name;
    private ReceiptResolverStrategyType resolver;
    private BigDecimal totalPrice;
    private LocalDateTime paymentDate;
    private Boolean isPreferredRevision;
    private Boolean isCorrect;
    private Collection<ReceiptItemDto> items;
    private UUID receiptId;
    private String revision;
}
