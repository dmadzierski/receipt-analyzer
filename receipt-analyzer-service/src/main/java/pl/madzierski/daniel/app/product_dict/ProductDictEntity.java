package pl.madzierski.daniel.app.product_dict;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.app.common.model.BaseEntity;
import pl.madzierski.daniel.app.product_dict.product_alias.ProductAliasEntity;
import pl.madzierski.daniel.app.product_dict.product_category.ProductCategoryEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "product_dict")
@Builder
@EqualsAndHashCode(callSuper = true)
public class ProductDictEntity extends BaseEntity {

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "productDict", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private final Set<ProductAliasEntity> aliases = new HashSet<>();
    @Column(nullable = false, unique = true)
    private String name;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_category_id")
    private ProductCategoryEntity productCategory;

    public void addAlias(ProductAliasEntity productAlias) {
        this.aliases.add(productAlias);
        productAlias.setProductDict(this);
    }

    public Set<ProductAliasEntity> getAliases() {
        return aliases.stream().collect(Collectors.toUnmodifiableSet());
    }
}
