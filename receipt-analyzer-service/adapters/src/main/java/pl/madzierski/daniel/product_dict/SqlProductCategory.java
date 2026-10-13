package pl.madzierski.daniel.product_dict;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.madzierski.daniel.user.SqlUserQuery;

import java.time.LocalDateTime;
import java.util.Set;


@EqualsAndHashCode(exclude = {"productDictList", "user"})
@Entity
@Table(name = "product_category")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EntityListeners({AuditingEntityListener.class})
class SqlProductCategory {

    @Id
    @UuidGenerator
    private String id;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

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
        return new SqlProductCategory(
            productCategory.getId(),
            productCategory.getCreatedDate(),
            productCategory.getModifiedDate(),
            productCategory.getName(),
            null,
            null
        );
    }
}