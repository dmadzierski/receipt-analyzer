package pl.madzierski.daniel.receipt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import pl.madzierski.daniel.digitization.ReceiptResolverStrategyType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Builder
public class ReceiptRevisionDto {
    private UUID id;
    private long version;
    private LocalDateTime createdDate;
    private ReceiptResolverStrategyType resolver;
    private BigDecimal totalPrice;
    private LocalDateTime paymentDate;
    private Boolean isPreferredRevision;
    private Boolean isCorrect;
    private UUID receiptId;
}
