package pl.madzierski.daniel.app.receipt.revision.item

import jakarta.persistence.*
import pl.madzierski.daniel.app.common.model.BaseEntity
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionEntity

@Entity
@Table(name = "item")
data class ItemEntity(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_revistion_id")
    val receiptRevision: ReceiptRevisionEntity,

    val name: String,

    val description: String

) : BaseEntity()