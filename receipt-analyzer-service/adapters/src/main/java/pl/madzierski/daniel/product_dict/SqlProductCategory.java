package pl.madzierski.daniel.product_dict;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.common.entity.VersionedEntity;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "product_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
class SqlProductCategory extends VersionedEntity {

    @Column(nullable = false, unique = true)
    private String name;

    public static SqlProductCategory fromProductCategory(ProductCategory productCategory) {
        SqlProductCategory sqlProductCategory = new SqlProductCategory(productCategory.getName());
        sqlProductCategory.setId(productCategory.getId());
        sqlProductCategory.setVersion(productCategory.getVersion());
        sqlProductCategory.setCreatedDate(productCategory.getCreatedDate());
        sqlProductCategory.setModifiedDate(productCategory.getModifiedDate());
        return sqlProductCategory;
    }

    public ProductCategory toProductCategory() {
        return ProductCategory.restore(id, version, createdDate, modifiedDate, name);
    }
}
