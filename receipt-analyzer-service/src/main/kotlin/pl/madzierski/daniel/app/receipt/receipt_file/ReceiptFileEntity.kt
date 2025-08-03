package pl.madzierski.daniel.app.receipt.receipt_file

import jakarta.persistence.*
import pl.madzierski.daniel.app.common.model.BaseEntity
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionEntity

@Entity
@Table(name = "receipt_file")
data class ReceiptFileEntity(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_revistion_id")
    val receiptRevision: ReceiptRevisionEntity,

    val path: String?

) : BaseEntity()
