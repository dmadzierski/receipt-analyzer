package pl.madzierski.daniel.app.file_group.file

import jakarta.persistence.*
import pl.madzierski.daniel.app.common.model.BaseEntity
import pl.madzierski.daniel.app.file_group.FileGroupEntity
import pl.madzierski.daniel.app.file_group.FileType
import pl.madzierski.daniel.app.receipt.ReceiptEntity

@Entity
@Table(name = "receipt_file")
data class FileEntity(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_group_id", nullable = true)
    val fileGroup: FileGroupEntity,

    var path: String? = null,

    @Column(name = "raw_data", columnDefinition = "TEXT")
    var rawData: String? = null,

    val partNumber: Int?

) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileEntity

        if (partNumber != other.partNumber) return false
        if (path != other.path) return false
        if (rawData != other.rawData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = partNumber ?: 0
        result = 31 * result + (path?.hashCode() ?: 0)
        result = 31 * result + (rawData?.hashCode() ?: 0)
        return result
    }

}
