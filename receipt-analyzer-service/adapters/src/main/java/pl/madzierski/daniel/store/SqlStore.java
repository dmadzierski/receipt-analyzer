package pl.madzierski.daniel.store;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.common.entity.BaseEntity;
import pl.madzierski.daniel.store.model.Store;


@Entity
@Table(name = "store")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = {"brand"})
class SqlStore extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private SqlStoreBrand brand;

    private String address;
    private String city;
    private String postalCode;
    private String country;

    public static SqlStore fromStore(Store store) {
        SqlStore sqlStore = new SqlStore(
                SqlStoreBrand.fromStoreBrand(store.getBrand()),
                store.getAddress(),
                store.getCity(),
                store.getPostalCode(),
                store.getCountry()
        );
        sqlStore.setId(store.getId());
        sqlStore.setCreatedDate(store.getCreatedDate());
        sqlStore.setModifiedDate(store.getModifiedDate());
        return sqlStore;
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
