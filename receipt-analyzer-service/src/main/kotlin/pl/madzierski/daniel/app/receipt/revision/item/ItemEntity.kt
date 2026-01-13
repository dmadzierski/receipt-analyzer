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

    var ptu: String?,

    var amount: Double?,

    var unitPrice: Double?,

    var discount: Double?,

    var totalPrice: Double?,

    var position: Int?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_item_id")
    var parentItem: ItemEntity? = null,

    @OneToMany(mappedBy = "parentItem")
    val childItems: MutableSet<ItemEntity> = mutableSetOf()


) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemEntity

        if (amount != other.amount) return false
        if (unitPrice != other.unitPrice) return false
        if (discount != other.discount) return false
        if (totalPrice != other.totalPrice) return false
        if (name != other.name) return false
        if (ptu != other.ptu) return false
        if (position != other.position) return false

        return true
    }

    override fun hashCode(): Int {
        var result = amount?.hashCode() ?: 0
        result = 31 * result + (unitPrice?.hashCode() ?: 0)
        result = 31 * result + (discount?.hashCode() ?: 0)
        result = 31 * result + (totalPrice?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (ptu?.hashCode() ?: 0)
        result = 31 * result + (position?.hashCode() ?: 0)
        return result
    }

}

