package pl.madzierski.daniel.store;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.madzierski.daniel.store.model.StoreBrand;

import java.time.LocalDateTime;

@Entity
@Table(name = "store_brand")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EntityListeners({AuditingEntityListener.class})
public class SqlStoreBrand {

    @Id
    @UuidGenerator
    private String id;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    private String name;

    public static SqlStoreBrand fromStoreBrand(StoreBrand brand) {
        return new SqlStoreBrand(
                brand.getId(),
                brand.getCreatedDate(),
                brand.getModifiedDate(),
                brand.getName()
        );
    }

    public StoreBrand toStoreBrand() {
        return new StoreBrand(
                this.id,
                this.name,
                this.createdDate,
                this.modifiedDate
        );
    }
}
