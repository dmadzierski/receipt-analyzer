package pl.madzierski.daniel.store;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.madzierski.daniel.common.entity.BaseEntity;


@Entity
@Table(name = "store_brand")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SqlStoreBrand extends BaseEntity {

    private String name;

    static SqlStoreBrand fromStoreBrand(StoreBrand brand) {
        SqlStoreBrand sqlStoreBrand = new SqlStoreBrand(brand.getName());
        sqlStoreBrand.setId(brand.getId());
        sqlStoreBrand.setCreatedDate(brand.getCreatedDate());
        sqlStoreBrand.setModifiedDate(brand.getModifiedDate());
        return sqlStoreBrand;
    }

    StoreBrand toStoreBrand() {
        return StoreBrand.restore(id, name, createdDate, modifiedDate);
    }
}
