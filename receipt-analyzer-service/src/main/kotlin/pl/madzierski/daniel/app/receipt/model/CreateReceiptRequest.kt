package pl.madzierski.daniel.app.receipt.model

import jakarta.validation.constraints.NotEmpty
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategyType
import java.time.LocalDateTime

data class CreateReceiptRequest(
    @NotEmpty
    val name: String,
    val description: String?,
    val brand: OCRHandlingResolver?,
    val date: LocalDateTime?,
    val strategy: ReceiptResolverStrategyType,
)
