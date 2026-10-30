package pl.madzierski.daniel.product_dict;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.common.entity.BaseEntity;
import pl.madzierski.daniel.user.SqlUserQuery;

import java.util.Set;


@EqualsAndHashCode(callSuper = true, exclude = {"productDictList", "user"})
@Entity
@Table(name = "product_category")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
class SqlProductCategory extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private SqlUserQuery user;

    @ManyToMany(mappedBy = "categories")
    private Set<SqlProductDict> productDictList;

    public static SqlProductCategory formProductCategory(ProductCategory productCategory) {
        SqlProductCategory sqlProductCategory = new SqlProductCategory();
        sqlProductCategory.setId(productCategory.getId());
        sqlProductCategory.setName(productCategory.getName());
        sqlProductCategory.setCreatedDate(productCategory.getCreatedDate());
        sqlProductCategory.setModifiedDate(productCategory.getModifiedDate());
        sqlProductCategory.setUser(productCategory.getUser() != null ? SqlUserQuery.fromUserQuery(productCategory.getUser()) : null);
        return sqlProductCategory;
    }



    public ProductCategory toProductCategory() {
        ProductCategory productCategory = new ProductCategory();
        productCategory.setId(this.id);
        productCategory.setName(this.name);
        productCategory.setCreatedDate(this.createdDate);
        productCategory.setModifiedDate(this.modifiedDate);
        return productCategory;
    }

    public static SqlProductCategory fromProductCategory(ProductCategory productCategory) {
        SqlProductCategory sqlProductCategory = new SqlProductCategory(productCategory.getName(), null, null);
        sqlProductCategory.setId(productCategory.getId());
        sqlProductCategory.setCreatedDate(productCategory.getCreatedDate());
        sqlProductCategory.setModifiedDate(productCategory.getModifiedDate());
        return sqlProductCategory;
    }
}