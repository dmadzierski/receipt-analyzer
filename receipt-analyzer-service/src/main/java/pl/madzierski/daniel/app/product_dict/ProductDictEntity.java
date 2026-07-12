package pl.madzierski.daniel.app.product_dict;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.app.common.model.BaseEntity;
import pl.madzierski.daniel.app.product_dict.product_alias.ProductAliasEntity;
import pl.madzierski.daniel.app.product_dict.product_category.ProductCategoryEntity;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "product_dict")
@Builder
@EqualsAndHashCode(callSuper = true)
public class ProductDictEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "productDict", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<ProductAliasEntity> aliases = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_category_id")
    private ProductCategoryEntity productCategory;

    public void addAlias(ProductAliasEntity productAlias) {
        if (this.aliases == null) {
            this.aliases = new HashSet<>();
        }
        this.aliases.add(productAlias);
        productAlias.setProductDict(this);
    }
}
