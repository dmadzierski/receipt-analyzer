package pl.madzierski.daniel.store;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pl.madzierski.daniel.common.entity.BaseEntity;
import pl.madzierski.daniel.store.model.StoreBrandQuery;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "store_brand")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class SqlStoreBrandQuery extends BaseEntity {

    SqlStoreBrandQuery(UUID id) {
        this.id = id;
    }

    static SqlStoreBrandQuery fromStoreBrandQuery(StoreBrandQuery brand) {
        return new SqlStoreBrandQuery(brand.id());
    }

    StoreBrandQuery toStoreBrandQuery() {
        return new StoreBrandQuery(id);
    }
}
