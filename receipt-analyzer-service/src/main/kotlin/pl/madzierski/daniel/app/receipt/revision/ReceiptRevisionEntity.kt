package pl.madzierski.daniel.app.receipt.revision

import jakarta.persistence.*
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import pl.madzierski.daniel.app.common.model.BaseEntity
import pl.madzierski.daniel.app.receipt.ReceiptEntity
import pl.madzierski.daniel.app.receipt.receipt_file.ReceiptFileEntity
import pl.madzierski.daniel.app.receipt.revision.item.ItemEntity

@Entity
@Table(name = "receipt_revision")
data class ReceiptRevisionEntity(

    val revision: String,

    val resolver: ScanResolver,

    @OneToMany(mappedBy = "receiptRevision")
    val items: List<ItemEntity>?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id")
    val receipt: ReceiptEntity,

    @OneToMany(mappedBy = "receiptRevision")
    val receiptFiles: List<ReceiptFileEntity>?,

    @Column(name = "raw_result")
    val rawResult: String?,

    @Column(name = "solution_rating", columnDefinition = "INT CHECK (solution_rating >= 0 AND solution_rating <= 10)")
    val solutionRating: Int?

) : BaseEntity()