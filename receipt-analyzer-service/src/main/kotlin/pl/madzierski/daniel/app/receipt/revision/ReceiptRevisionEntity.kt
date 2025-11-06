package pl.madzierski.daniel.app.receipt.revision

import jakarta.persistence.*
import pl.madzierski.daniel.app.common.model.BaseEntity
import pl.madzierski.daniel.app.receipt.ReceiptEntity
import pl.madzierski.daniel.app.receipt.model.Brand
import pl.madzierski.daniel.app.receipt.revision.item.ItemEntity
import pl.madzierski.daniel.app.receipt.revision.receipt_file.ReceiptFileEntity

@Entity
@Table(name = "receipt_revision")
data class ReceiptRevisionEntity(

    var revision: String?,

    var resolver: ScanResolver?,

    var brand: Brand?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id")
    var receipt: ReceiptEntity,

    @OneToMany(mappedBy = "receiptRevision")
    val receiptFiles: MutableList<ReceiptFileEntity>,

    @OneToMany(mappedBy = "receiptRevision")
    val items: MutableList<ItemEntity>,

    var totalPrice: Double?,

    var payingDate: String?,

    var address: String?,

    var preferredRevision: Boolean?

) : BaseEntity()