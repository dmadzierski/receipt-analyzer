package pl.madzierski.daniel.app.receipt.revision.model

import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionEntity
import pl.madzierski.daniel.app.receipt.revision.ScanResolver
import pl.madzierski.daniel.app.receipt.revision.item.ItemEntity
import pl.madzierski.daniel.app.receipt.revision.receipt_file.ReceiptFileEntity
import java.time.LocalDateTime


data class GetRevisionResponse(
    var id: String?,
    var resolver: ScanResolver?,
    val createdDate: LocalDateTime?,
    val brand: String?,
    val totalPrice: Double?,
    val payingDate: String?,
    val address: String?,
    val items: Set<ItemResponse>?,
    val files: Set<FileResponse>?

) {

    data class ItemResponse(
        val id: String?,
        val name: String?,
        val ptu: String?,
        val amount: Double?,
        val unitPrice: Double?,
        val discount: Double?,
        val totalPrice: Double?,
        val position: Int?,
    )

    data class FileResponse(
        val id: String?, val path: String?, val rawData: String?
    )

    companion object {

        fun revisionMapper(revisionEntity: ReceiptRevisionEntity?): GetRevisionResponse? {
            if (revisionEntity != null) {
                return GetRevisionResponse(
                    revisionEntity.id,
                    revisionEntity.resolver,
                    revisionEntity.createdDate,
                    revisionEntity.brand,
                    revisionEntity.totalPrice,
                    revisionEntity.payingDate,
                    revisionEntity.address,
                    revisionEntity.items.mapTo(mutableSetOf()) { itemMapper(it) },
                    revisionEntity.receiptFiles.mapTo(mutableSetOf()) { fileMapper(it) })
            }
            return null
        }

        fun fileMapper(file: ReceiptFileEntity) = FileResponse(
            file.id, file.path, file.rawData
        )

        fun itemMapper(itemEntity: ItemEntity): ItemResponse = ItemResponse(
            itemEntity.id,
            itemEntity.name,
            itemEntity.ptu,
            itemEntity.amount,
            itemEntity.unitPrice,
            itemEntity.discount,
            itemEntity.totalPrice,
            itemEntity.position
        )

    }
}
