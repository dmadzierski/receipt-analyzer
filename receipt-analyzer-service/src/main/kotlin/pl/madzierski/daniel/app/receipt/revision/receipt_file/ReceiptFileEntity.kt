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

) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReceiptFileEntity

        if (isOriginal != other.isOriginal) return false
        if (receiptRevision != other.receiptRevision) return false
        if (path != other.path) return false
        if (rawData != other.rawData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isOriginal.hashCode()
        result = 31 * result + (path?.hashCode() ?: 0)
        result = 31 * result + (rawData?.hashCode() ?: 0)
        return result
    }
}
