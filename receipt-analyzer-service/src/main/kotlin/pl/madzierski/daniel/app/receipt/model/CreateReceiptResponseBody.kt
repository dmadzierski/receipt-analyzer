package pl.madzierski.daniel.app.receipt.model

data class CreateReceiptResponseBody(
    val id: String?,
    val name: String?,
    val description: String?,
    val receiptRevision: List<CreateReceiptReceiptRevisionResponseBody>
)

data class CreateReceiptReceiptRevisionResponseBody(
    val id: String?,
    val revision: String?,
    val resolver: String?,
    val receiptFile: List<CreateReceiptReceiptFileResponseBody>
)

data class CreateReceiptReceiptFileResponseBody(val id: String?, val path: String?)
