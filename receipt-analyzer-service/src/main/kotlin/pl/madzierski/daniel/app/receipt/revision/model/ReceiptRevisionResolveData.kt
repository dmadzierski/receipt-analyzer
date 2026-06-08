package pl.madzierski.daniel.app.receipt.revision.model

data class ReceiptRevisionResolveData(
    var revisionVersion: String?,
    var brand: String?,
    var items: List<ReceiptRevisionResolveDataItem>?,
    var files: List<ReceiptRevisionResolveDataFile>?
) {
    data class ReceiptRevisionResolveDataItem(
        var name: String?,
        var amount: Double?,
        var unitPrice: Double?,
        var discount: Double?,
        var totalPrice: Double?,
        var position: Int?,
    )

    data class ReceiptRevisionResolveDataFile(
        var name: String,
        var page: Int,
        var rawData: String,
    )
}
