package pl.madzierski.daniel.app.receipt.model

import java.util.*

data class GetReceiptListResponse(
    val items: List<GetReceiptListItemResponse>
){
    data class GetReceiptListItemResponse(
        val id: String,
        val name: String?,
        val description: String?,
        val createDate: Date?,
    )
}