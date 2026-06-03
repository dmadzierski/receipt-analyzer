package pl.madzierski.daniel.app.file_group

import jakarta.persistence.*
import pl.madzierski.daniel.app.common.model.BaseEntity
import pl.madzierski.daniel.app.file_group.file.FileEntity
import pl.madzierski.daniel.app.receipt.ReceiptEntity

@Entity
@Table(name = "receipt_file_group")
data class FileGroupEntity(

    var fileType: FileType?,

    @ManyToOne
    @JoinColumn(name = "receipt_id")
    val receipt: ReceiptEntity,

    val isOriginal: Boolean? = null,

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    var files: MutableSet<FileEntity> = mutableSetOf(),

) : BaseEntity()
