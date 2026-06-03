package pl.madzierski.daniel.app.receipt

import jakarta.persistence.*
import pl.madzierski.daniel.app.common.model.BaseEntity
import pl.madzierski.daniel.app.file_group.FileGroupEntity
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionEntity

@Entity
@Table(name = "receipt")
data class ReceiptEntity(

    var name: String,

    var description: String?,

    @Column(name = "user_sub")
    var userSub: String,

    @OneToMany(mappedBy = "receipt")
    val receiptRevisions: MutableSet<ReceiptRevisionEntity>,

    @OneToMany(mappedBy = "receipt")
    var fileGroupEntity: MutableSet<FileGroupEntity> = mutableSetOf(),

    ) : BaseEntity()