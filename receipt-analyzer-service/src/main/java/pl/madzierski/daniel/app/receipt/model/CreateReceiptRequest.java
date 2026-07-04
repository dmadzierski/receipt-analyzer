package pl.madzierski.daniel.app.receipt.model;

import jakarta.validation.constraints.NotNull;
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategyType;

import java.time.LocalDateTime;

public record CreateReceiptRequest(
        String name,
        String description,
        OCRHandlingResolver brand,
        LocalDateTime date,
        ReceiptResolverStrategyType strategy,
        @NotNull
        String walletId
) {
}