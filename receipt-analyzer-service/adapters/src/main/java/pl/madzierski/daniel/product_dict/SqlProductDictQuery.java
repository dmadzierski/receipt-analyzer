package pl.madzierski.daniel.product_dict;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pl.madzierski.daniel.product_dict.model.ProductDictQuery;

@EqualsAndHashCode
@Entity
@Table(name = "product")
@AllArgsConstructor
@NoArgsConstructor
public class SqlProductDictQuery {

    @Id
    private String id;

    public static SqlProductDictQuery fromProductDict(ProductDictQuery nameDict) {
        return new SqlProductDictQuery(nameDict.getId());
    }
}
