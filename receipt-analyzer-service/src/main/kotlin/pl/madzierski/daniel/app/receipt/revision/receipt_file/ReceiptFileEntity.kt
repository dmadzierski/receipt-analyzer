package pl.madzierski.daniel.app.receipt.revision.receipt_file

import jakarta.persistence.*
import pl.madzierski.daniel.app.common.model.BaseEntity
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionEntity

@Entity
@Table(name = "receipt_file")
data class ReceiptFileEntity(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_revision_id")
    val receiptRevision: ReceiptRevisionEntity,

    var path: String? = null,

    val isOriginal: Boolean = false,

    @Column(name = "raw_data", columnDefinition = "TEXT")
    var rawData: String? = null

) : BaseEntity()
