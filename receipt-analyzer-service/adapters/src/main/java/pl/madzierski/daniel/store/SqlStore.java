package pl.madzierski.daniel.store;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.common.entity.VersionedEntity;


@Entity
@Table(name = "store")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = {"brand"})
class SqlStore extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private SqlStoreBrandQuery brand;

    private String address;
    private String city;
    private String postalCode;
    private String country;

    static SqlStore fromStore(Store store) {
        SqlStore sqlStore = new SqlStore(
                store.getBrand() != null ? SqlStoreBrandQuery.fromStoreBrandQuery(store.getBrand()) : null,
                store.getAddress(),
                store.getCity(),
                store.getPostalCode(),
                store.getCountry()
        );
        sqlStore.setId(store.getId());
        sqlStore.setVersion(store.getVersion());
        sqlStore.setCreatedDate(store.getCreatedDate());
        sqlStore.setModifiedDate(store.getModifiedDate());
        return sqlStore;
    }

    Store toStore() {
        return Store.restore(id, version, brand != null ? brand.toStoreBrandQuery() : null, address, city, postalCode, country,
            createdDate, modifiedDate);
    }
}
