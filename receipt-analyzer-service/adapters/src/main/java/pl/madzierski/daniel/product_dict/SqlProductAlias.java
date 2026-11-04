package pl.madzierski.daniel.product_dict;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.common.entity.BaseEntity;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "product_alias")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
class SqlProductAlias extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    public static SqlProductAlias fromProductAlias(ProductAlias productAlias) {
        SqlProductAlias sqlProductAlias = new SqlProductAlias(productAlias.getName());
        sqlProductAlias.setId(productAlias.getId());
        sqlProductAlias.setCreatedDate(productAlias.getCreatedDate());
        sqlProductAlias.setModifiedDate(productAlias.getModifiedDate());
        return sqlProductAlias;
    }

    public ProductAlias toProductAlias() {
        return new ProductAlias(
            this.id,
            this.createdDate,
            this.modifiedDate,
            this.name
        );
    }
}
