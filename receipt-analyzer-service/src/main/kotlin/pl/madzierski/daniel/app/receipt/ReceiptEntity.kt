package pl.madzierski.daniel.app.receipt

import jakarta.persistence.*
import pl.madzierski.daniel.app.common.model.BaseEntity
import pl.madzierski.daniel.app.file_group.FileGroupEntity
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionEntity

@Entity
@Table(name = "receipt")
class ReceiptEntity(

    var name: String,

    var description: String?,

    @Column(name = "user_sub")
    var userSub: String,

    @OneToMany(mappedBy = "receipt", cascade = [CascadeType.ALL])
    var receiptRevisions: MutableSet<ReceiptRevisionEntity> = mutableSetOf(),

    @OneToMany(mappedBy = "receipt")
    var fileGroupEntity: MutableSet<FileGroupEntity> = mutableSetOf(),

    ) : BaseEntity() {
    fun addRevision(toReceiptRevisionEntity: ReceiptRevisionEntity) {
        this.receiptRevisions.add(toReceiptRevisionEntity)
    }
}