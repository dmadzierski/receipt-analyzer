package pl.madzierski.daniel.app.receipt.revision.model

import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionEntity
import pl.madzierski.daniel.app.receipt.revision.item.ItemEntity
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategyType
import java.time.LocalDateTime

data class AddRevisionRequest(
    val receiptId: String,
    val createdDate: LocalDateTime?,
    val brand: String?,
    val totalPrice: Double?,
    val payingDate: String?,
    val address: String?,
    val items: Set<AddRevisionItemRequest>?
) {
    data class AddRevisionItemRequest(
        val id: String,
        val name: String?,
        val amount: Double?,
        val unitPrice: Double?,
        val discount: Double?,
        val totalPrice: Double?,
        val position: Int?,
        val originalItemId: String?
    )
}

data class AddRevisionResponse(
    var revision: String?,
    var resolver: ReceiptResolverStrategyType?,
    val receiptId: String,
    val createdDate: LocalDateTime?,
    val brand: String?,
    val totalPrice: Double?,
    val payingDate: String?,
    val address: String?,
    val items: Set<AddRevisionItemResponse>?
) {
    data class AddRevisionItemResponse(
        val id: String?,
        val name: String?,
        val amount: Double?,
        val unitPrice: Double?,
        val discount: Double?,
        val totalPrice: Double?,
        val position: Int?,
        val originalItemId: String?
    )

    companion object {
        fun addRevisionMapper(receiptRevisionEntity: ReceiptRevisionEntity) = AddRevisionResponse(
            receiptRevisionEntity.id,
            receiptRevisionEntity.resolver,
            receiptRevisionEntity.receipt?.id!!,
            receiptRevisionEntity.createdDate,
            receiptRevisionEntity.brand,
            receiptRevisionEntity.totalPrice,
            receiptRevisionEntity.payingDate,
            receiptRevisionEntity.address,
            receiptRevisionEntity.items.map { addRevisionItemMapper(it) }.toMutableSet()
        )

        fun addRevisionItemMapper(itemEntity: ItemEntity): AddRevisionItemResponse = AddRevisionItemResponse(
            itemEntity.id,
            itemEntity.name,
            itemEntity.amount,
            itemEntity.unitPrice,
            itemEntity.discount,
            itemEntity.totalPrice,
            itemEntity.position,
            itemEntity.parentItem?.id
        )
    }
}