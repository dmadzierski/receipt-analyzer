package pl.madzierski.daniel.app.receipt.model

import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionEntity
import pl.madzierski.daniel.app.receipt.revision.ScanResolver
import java.time.LocalDateTime

data class GetReceiptRevisionsResponse(
    var id: String?,
    var resolver: ScanResolver?,
    val createdDate: LocalDateTime?,
    val brand: String?,
    val totalPrice: Double?,
    val payingDate: String?,
    val address: String?,
    val isPreferredRevision: Boolean?,
    val isCorrect: Boolean?,

    ) {
    companion object {
        fun receiptRevisionMapper(revision: ReceiptRevisionEntity): GetReceiptRevisionsResponse =
            GetReceiptRevisionsResponse(
                revision.id,
                revision.resolver,
                revision.createdDate,
                revision.brand,
                revision.totalPrice,
                revision.payingDate,
                revision.address,
                revision.isPreferredRevision,
                revision.isCorrect
            )


    }
}