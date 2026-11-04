package pl.madzierski.daniel.product_dict;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.common.entity.BaseEntity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "product")
@EqualsAndHashCode(callSuper = true, exclude = {"categories", "aliases"})
class SqlProductDict extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;
    @Getter(AccessLevel.NONE)
    @ManyToMany
    @JoinTable(
        name = "product_category_mapping",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private final Set<SqlProductCategory> categories = new HashSet<>();
    @Getter(AccessLevel.NONE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private final Set<SqlProductAlias> aliases = new HashSet<>();

    public Set<SqlProductAlias> getAliases() {
        return Collections.unmodifiableSet(aliases);
    }

    public Set<SqlProductCategory> getCategories() {
        return Collections.unmodifiableSet(categories);
    }

    public static <S extends ProductDict> SqlProductDict fromProductDict(S s) {
        SqlProductDict sqlProductDict = new SqlProductDict();
        sqlProductDict.setId(s.getId());
        sqlProductDict.setName(s.getName());
        sqlProductDict.setCreatedDate(s.getCreatedDate());
        sqlProductDict.setModifiedDate(s.getModifiedDate());
        sqlProductDict.aliases.addAll(s.getAliases()
            .stream()
            .map(SqlProductAlias::fromProductAlias)
            .toList());
        sqlProductDict.categories.addAll(s.getProductCategories()
            .stream()
            .map(SqlProductCategory::fromProductCategory)
            .toList());
        return sqlProductDict;
    }

    public ProductDict toProductDict() {
        ProductDict productDict = new ProductDict();
        productDict.setId(this.id);
        productDict.setName(this.name);
        productDict.setCreatedDate(this.createdDate);
        productDict.setModifiedDate(this.modifiedDate);
        productDict.addAllAlias(this.aliases.stream().map(SqlProductAlias::toProductAlias).toList());
        productDict.addAllCategories(this.categories.stream().map(SqlProductCategory::toProductCategory).toList());
        return productDict;
    }

}
