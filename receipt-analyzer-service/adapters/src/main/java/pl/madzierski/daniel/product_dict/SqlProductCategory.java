package pl.madzierski.daniel.product_dict;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.madzierski.daniel.user.SqlUser;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;


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
    private SqlUser user;

    @ManyToMany(mappedBy = "categories")
    private Set<SqlProductDict> productDictList;

    public static SqlProductCategory formProductCategory(ProductCategory productCategory) {
        SqlProductCategory sqlProductCategory = new SqlProductCategory();
        sqlProductCategory.setId(productCategory.getId());
        sqlProductCategory.setName(productCategory.getName());
        sqlProductCategory.setCreatedDate(productCategory.getCreatedDate());
        sqlProductCategory.setModifiedDate(productCategory.getModifiedDate());
        return sqlProductCategory;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SqlProductCategory that = (SqlProductCategory) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(name);
        return result;
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