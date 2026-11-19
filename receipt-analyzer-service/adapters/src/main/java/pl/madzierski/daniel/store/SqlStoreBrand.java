package pl.madzierski.daniel.store;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.madzierski.daniel.common.entity.VersionedEntity;


@Entity
@Table(name = "store_brand")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SqlStoreBrand extends VersionedEntity {

    private String name;

    static SqlStoreBrand fromStoreBrand(StoreBrand brand) {
        SqlStoreBrand sqlStoreBrand = new SqlStoreBrand(brand.getName());
        sqlStoreBrand.setId(brand.getId());
        sqlStoreBrand.setVersion(brand.getVersion());
        sqlStoreBrand.setCreatedDate(brand.getCreatedDate());
        sqlStoreBrand.setModifiedDate(brand.getModifiedDate());
        return sqlStoreBrand;
    }

    StoreBrand toStoreBrand() {
        return StoreBrand.restore(id, version, name, createdDate, modifiedDate);
    }
}
