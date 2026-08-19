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
import pl.madzierski.daniel.store.model.Store;

import java.time.LocalDateTime;

@Entity
@Table(name = "store")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EntityListeners({AuditingEntityListener.class})
class SqlStore {

    @Id
    @UuidGenerator
    private String id;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private SqlStoreBrand brand;

    private String address;
    private String city;
    private String postalCode;
    private String country;

    public static SqlStore fromStore(Store store) {
        return new SqlStore(
                store.getId(),
                store.getCreatedDate(),
                store.getModifiedDate(),
                SqlStoreBrand.fromStoreBrand(store.getBrand()),
                store.getAddress(),
                store.getCity(),
                store.getPostalCode(),
                store.getCountry()
        );
    }

    public Store toStore() {
        return new Store(
                this.id,
                this.brand.toStoreBrand(),
                this.address,
                this.city,
                this.postalCode,
                this.country,
                this.createdDate,
                this.modifiedDate
        );
    }
}
