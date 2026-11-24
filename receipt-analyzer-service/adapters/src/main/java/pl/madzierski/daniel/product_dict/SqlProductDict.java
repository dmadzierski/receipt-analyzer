package pl.madzierski.daniel.product_dict;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.common.entity.VersionedEntity;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter(AccessLevel.PRIVATE)
@Entity
@Table(name = "product")
@EqualsAndHashCode(callSuper = true, exclude = {"categories", "aliases"})
class SqlProductDict extends VersionedEntity {

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

    static SqlProductDict fromProductDict(ProductDict productDict) {
        SqlProductDict sqlProductDict = new SqlProductDict();
        sqlProductDict.setId(productDict.getId());
        sqlProductDict.setVersion(productDict.getVersion());
        sqlProductDict.setName(productDict.getName());
        sqlProductDict.setCreatedDate(productDict.getCreatedDate());
        sqlProductDict.setModifiedDate(productDict.getModifiedDate());
        sqlProductDict.aliases.addAll(productDict.getAliases()
            .stream()
            .map(SqlProductAlias::fromProductAlias)
            .toList());
        sqlProductDict.categories.addAll(productDict.getProductCategories()
            .stream()
            .map(SqlProductCategory::fromProductCategory)
            .toList());
        return sqlProductDict;
    }

    ProductDict toProductDict() {
        return ProductDict.restore(id, version, createdDate, modifiedDate, name,
            aliases.stream().map(SqlProductAlias::toProductAlias).toList(),
            categories.stream().map(SqlProductCategory::toProductCategory).toList());
    }

}
