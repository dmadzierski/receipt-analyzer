package pl.madzierski.daniel.app.receipt

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import pl.madzierski.daniel.app.common.model.BaseEntity
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionEntity

@Entity
@Table(name = "receipt")
data class ReceiptEntity(

    var name: String,

    var description: String?,

    @Column(name = "user_sub")
    var userSub: String,

    @OneToMany(mappedBy = "receipt")
    val receiptRevisions: MutableSet<ReceiptRevisionEntity>


) : BaseEntity()