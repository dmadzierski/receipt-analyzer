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

    val name: String,

    val description: String?,

    @Column(name = "user_sub")
    val userSub: String,

    @OneToMany(mappedBy = "receipt")
    val receiptRevisions: List<ReceiptRevisionEntity>?

) : BaseEntity()