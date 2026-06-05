package pl.madzierski.daniel.app.receipt.revision

import jakarta.persistence.*
import pl.madzierski.daniel.app.common.model.BaseEntity
import pl.madzierski.daniel.app.receipt.ReceiptEntity
import pl.madzierski.daniel.app.receipt.revision.item.ItemEntity
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategyType

@Entity
@Table(name = "receipt_revision")
class ReceiptRevisionEntity(

    var name: String?,

    var revision: String?,

    var resolver: ReceiptResolverStrategyType?,

    var brand: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id")
    var receipt: ReceiptEntity?,

    @OneToMany(mappedBy = "receiptRevision", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val items: MutableSet<ItemEntity> = mutableSetOf(),

    var totalPrice: Double?,

    var payingDate: String?,

    var address: String?,

    var isPreferredRevision: Boolean?,

    var isCorrect: Boolean?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_receipt_revision_id")
    var parentReceiptRevision: ReceiptRevisionEntity? = null,

    @OneToMany(mappedBy = "parentReceiptRevision")
    val childReceiptRevisions: MutableSet<ReceiptRevisionEntity> = mutableSetOf()

) : BaseEntity() {

    fun addItem(item: ItemEntity) {
        item.receiptRevision = this
        items.add(item)
    }

    fun addItems(items: Collection<ItemEntity>) {
        items.forEach { it.receiptRevision = this }
        this.items.addAll(items)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReceiptRevisionEntity

        if (totalPrice != other.totalPrice) return false
        if (isPreferredRevision != other.isPreferredRevision) return false
        if (isCorrect != other.isCorrect) return false
        if (revision != other.revision) return false
        if (resolver != other.resolver) return false
        if (brand != other.brand) return false
        if (items != other.items) return false
        if (payingDate != other.payingDate) return false
        if (address != other.address) return false

        return true
    }

    override fun hashCode(): Int {
        var result = totalPrice?.hashCode() ?: 0
        result = 31 * result + (isPreferredRevision?.hashCode() ?: 0)
        result = 31 * result + (isCorrect?.hashCode() ?: 0)
        result = 31 * result + (resolver?.hashCode() ?: 0)
        result = 31 * result + (brand?.hashCode() ?: 0)
        result = 31 * result + if (items != null) items.hashCode() else 0
        result = 31 * result + (payingDate?.hashCode() ?: 0)
        result = 31 * result + (address?.hashCode() ?: 0)
        return result
    }
}