package pl.madzierski.daniel.product_dict;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "product")
@EqualsAndHashCode
@EntityListeners({AuditingEntityListener.class})
class SqlProductDict {
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
    @Getter(AccessLevel.NONE)
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "product_category_mapping",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private final Set<SqlProductCategory> categories = new HashSet<>();
    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "productDict", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final Set<SqlProductAlias> aliases = new HashSet<>();

    public static <S extends ProductDict> SqlProductDict fromProductDict(S s) {
        SqlProductDict sqlProductDict = new SqlProductDict();
        sqlProductDict.setId(s.getId());
        sqlProductDict.setName(s.getName());
        sqlProductDict.setCreatedDate(s.getCreatedDate());
        sqlProductDict.setModifiedDate(s.getModifiedDate());
        return sqlProductDict;
    }

    public ProductDict toProductDict() {
        ProductDict productDict = new ProductDict();
        productDict.setId(this.id);
        productDict.setName(this.name);
        productDict.setCreatedDate(this.createdDate);
        productDict.setModifiedDate(this.modifiedDate);
        return productDict;
    }
}
