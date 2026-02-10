package pl.madzierski.daniel.app.common.model

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.UuidGenerator
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity(

    @Id
    @UuidGenerator
    var id: String? = null,

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    var createdDate: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "modified_date")
    var modifiedDate: LocalDateTime = LocalDateTime.now()
)