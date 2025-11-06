package pl.madzierski.daniel.app.receipt.revision.item

import jakarta.persistence.*
import pl.madzierski.daniel.app.common.model.BaseEntity
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionEntity

@Entity
@Table(name = "receipt_item")
data class ItemEntity(

    @ManyToOne(fetch = FetchType.LAZY)
    var receiptRevision: ReceiptRevisionEntity?,

    var name: String?,

    var vat: String?,

    var amount: Double?,

    var unitPrice: Double?,

    var discount: Double?,

    var totalPrice: Double?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_item_id")
    private var parentItem: ItemEntity? = null,

    @OneToMany(mappedBy = "parentItem")
    val childItems: MutableList<ItemEntity> = mutableListOf()

) : BaseEntity()

