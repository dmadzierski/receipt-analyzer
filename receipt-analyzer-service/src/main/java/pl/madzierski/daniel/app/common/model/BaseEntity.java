package pl.madzierski.daniel.app.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;


@MappedSuperclass
@EntityListeners({AuditingEntityListener.class})
@Data

public abstract class BaseEntity {
    @Id
    @UuidGenerator
    private String id;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        BaseEntity that = (BaseEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(createdDate, that.createdDate) && Objects.equals(modifiedDate, that.modifiedDate);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(createdDate);
        result = 31 * result + Objects.hashCode(modifiedDate);
        return result;
    }
}

