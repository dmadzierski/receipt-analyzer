package pl.madzierski.daniel.app.receipt.revision.model

import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategyType
import java.time.LocalDateTime

data class UpdateRevisionRequest(
    var id: String?,
    var resolver: ReceiptResolverStrategyType?,
    val createdDate: LocalDateTime?,
    val brand: String?,
    val totalPrice: Double?,
    val payingDate: String?,
    val address: String?,
    val isPreferredRevision: Boolean?,
    val isCorrect: Boolean?,
    val items: Set<ItemResponse>?,
) {

    data class ItemResponse(
        val id: String?,
        val name: String?,
        val amount: Double?,
        val unitPrice: Double?,
        val discount: Double?,
        val totalPrice: Double?,
        val position: Int?,
    )

}
