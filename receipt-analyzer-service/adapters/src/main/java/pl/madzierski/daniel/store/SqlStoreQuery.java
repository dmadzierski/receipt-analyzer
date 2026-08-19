package pl.madzierski.daniel.store;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import pl.madzierski.daniel.store.model.StoreQuery;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SqlStoreQuery that = (SqlStoreQuery) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(id);
        return result;
    }
}