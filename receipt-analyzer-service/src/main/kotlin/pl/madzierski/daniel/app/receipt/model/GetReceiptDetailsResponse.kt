package pl.madzierski.daniel.app.receipt.model

import pl.madzierski.daniel.app.file_group.FileType
import pl.madzierski.daniel.app.receipt.ReceiptEntity
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionEntity
import pl.madzierski.daniel.app.receipt.revision.ScanResolver
import pl.madzierski.daniel.app.receipt.revision.item.ItemEntity
import java.time.LocalDateTime

data class GetReceiptDetailsResponse(
    val id: String?,
    val name: String?,
    val description: String?,
    val preferredRevision: PreferredRevisionResponse?,
    val revisions: Set<RevisionResponse>,
    val createDate: LocalDateTime?,
    val updateDate: LocalDateTime?,
    val fileId: String?,
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

    data class PreferredRevisionResponse(
        var id: String?,
        var resolver: ScanResolver?,
        val createdDate: LocalDateTime?,
        val brand: String?,
        val totalPrice: Double?,
        val payingDate: String?,
        val address: String?,
        val isPreferredRevision: Boolean?,
        val isCorrect: Boolean?,
        val items: Set<ItemResponse>?
    )

    data class RevisionResponse(
        var id: String?,
        var resolver: ScanResolver?,
        val createdDate: LocalDateTime?,
        val brand: String?,
        val totalPrice: Double?,
        val payingDate: String?,
        val address: String?,
        val isPreferredRevision: Boolean?,
        val isCorrect: Boolean?,
    )

    data class FileResponse(
        val id: String?, val path: String?, val rawData: String?
    )

    companion object {
        fun receiptDetailsMapper(
            receiptEntity: ReceiptEntity,
            preferredRevisionEntity: ReceiptRevisionEntity?,
            fileId: String
        ) =
            GetReceiptDetailsResponse(
                receiptEntity.id,
                receiptEntity.name,
                receiptEntity.description,
                preferredRevisionMapper(preferredRevisionEntity),
                receiptRevisionMapper(receiptEntity.receiptRevisions),
                receiptEntity.createdDate,
                receiptEntity.modifiedDate,
                fileId
            )

        fun preferredRevisionMapper(revisionEntity: ReceiptRevisionEntity?): PreferredRevisionResponse? {
            if (revisionEntity != null) {
                return PreferredRevisionResponse(
                    revisionEntity.id,
                    revisionEntity.resolver,
                    revisionEntity.createdDate,
                    revisionEntity.brand,
                    revisionEntity.totalPrice,
                    revisionEntity.payingDate,
                    revisionEntity.address,
                    revisionEntity.isPreferredRevision,
                    revisionEntity.isCorrect,
                    revisionEntity.items.mapTo(mutableSetOf()) { itemMapper(it) }
                )
            }
            return null;
        }

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

        fun receiptRevisionMapper(receiptRevisions: Set<ReceiptRevisionEntity>): Set<RevisionResponse> =
            if (receiptRevisions.isNotEmpty()) receiptRevisions.mapTo(mutableSetOf()) {
                RevisionResponse(
                    it.id,
                    it.resolver,
                    it.createdDate,
                    it.brand,
                    it.totalPrice,
                    it.payingDate,
                    it.address,
                    it.isPreferredRevision,
                    it.isCorrect
                )
            }
            else emptySet()

    }
}


