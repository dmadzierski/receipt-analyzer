package pl.madzierski.daniel.app.receipt.model

import pl.madzierski.daniel.app.receipt.revision.ScanResolver
import java.time.LocalDateTime

data class GetReceiptDetailsResponse(
    val id: String?,
    val name: String?,
    val description: String?,
    val preferredRevision: PreferredRevisionResponse?,
    val revisions: Set<RevisionResponse>,
    val createDate: LocalDateTime?,
    val updateDate: LocalDateTime?
) {
    data class ItemResponse(
        val id: String?,
        val name: String?,
        val vat: String?,
        val amount: Double?,
        val unitPrice: Double?,
        val discount: Double?,
        val totalPrice: Double?,
        val position: Int?,
    )

    data class PreferredRevisionResponse(
        var id: String?, var resolver: ScanResolver?, val createdDate: LocalDateTime?, val items: Set<ItemResponse>?, val files: Set<FileResponse>?
    )

    data class RevisionResponse(
        var id: String?, var resolver: ScanResolver?, val createdDate: LocalDateTime?
    )

    data class FileResponse(
        val id: String?,
        val path: String?,
        val rawData: String?
    )

}

data class GetReceiptDetailsRevisionDetailsResponse(
    var id: String?,
    var brand: Brand?,
    val receiptFiles: List<GetReceiptDetailsFileResponse>,
    val items: List<GetReceiptDetailsItemResponse>,
    var totalPrice: Double?,
    var payingDate: String?,
    var address: String?
)

data class GetReceiptDetailsFileResponse(
    val id: String
)

data class GetReceiptDetailsItemResponse(
    val id: String,
    val name: String?,
    val vat: String?,
    val amount: Double?,
    val unitPrice: Double?,
    val discount: Double?,
    val totalPrice: Double?,
)



