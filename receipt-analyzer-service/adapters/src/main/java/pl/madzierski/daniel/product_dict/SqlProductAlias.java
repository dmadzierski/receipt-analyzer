package pl.madzierski.daniel.product_dict;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.common.entity.BaseEntity;

@EqualsAndHashCode(callSuper = true, exclude = "productDict")
@Entity
@Table(name = "product_alias")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
class SqlProductAlias extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private SqlProductDict productDict;



    public static SqlProductAlias fromProductAlias(ProductAlias productAlias, SqlProductDict sqlProductDict) {
        SqlProductAlias sqlProductAlias = new SqlProductAlias(productAlias.getName(), sqlProductDict);
        sqlProductAlias.setId(productAlias.getId());
        sqlProductAlias.setCreatedDate(productAlias.getCreatedDate());
        sqlProductAlias.setModifiedDate(productAlias.getModifiedDate());
        return sqlProductAlias;
    }

    public ProductAlias toProductAlias(ProductDict productDict) {
        return new ProductAlias(
            this.id,
            this.createdDate,
            this.modifiedDate,
            this.name,
            productDict
        );
    }
}