package pl.madzierski.daniel.product_dict;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pl.madzierski.daniel.common.entity.BaseEntity;
import pl.madzierski.daniel.product_dict.model.ProductDictQuery;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "product")
@NoArgsConstructor
public class SqlProductDictQuery extends BaseEntity {

    public SqlProductDictQuery(UUID id) {
        this.id = id;
    }


    public static SqlProductDictQuery fromProductDict(ProductDictQuery nameDict) {
        return new SqlProductDictQuery(nameDict.getId());
    }

    public ProductDictQuery toProductDictQuery() {
        return new ProductDictQuery(id);
    }
}
