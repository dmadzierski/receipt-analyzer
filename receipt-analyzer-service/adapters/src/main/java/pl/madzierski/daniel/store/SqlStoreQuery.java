package pl.madzierski.daniel.store;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pl.madzierski.daniel.common.entity.BaseEntity;
import pl.madzierski.daniel.store.model.StoreQuery;


@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "store")
@NoArgsConstructor
public class SqlStoreQuery extends BaseEntity {

    public SqlStoreQuery(String id) {
        this.id = id;
    }

    public static SqlStoreQuery fromStoreQuery(StoreQuery store) {
        return new SqlStoreQuery(store.getId());
    }

    public StoreQuery toStoreQuery() {
        return new StoreQuery(id);
    }


}