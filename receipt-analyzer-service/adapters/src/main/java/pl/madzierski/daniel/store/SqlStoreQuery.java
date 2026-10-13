package pl.madzierski.daniel.store;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import pl.madzierski.daniel.store.model.StoreQuery;


@EqualsAndHashCode
@Entity
@Table(name = "store")
@NoArgsConstructor
@AllArgsConstructor
public class SqlStoreQuery {
    @Id
    @UuidGenerator
    private String id;

    public static SqlStoreQuery fromStoreQuery(StoreQuery store) {
        return new SqlStoreQuery(store.getId());
    }


}