package pl.madzierski.daniel.app.product_dict;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.app.common.model.BaseEntity;
import pl.madzierski.daniel.app.product_dict.product_alias.ProductAliasEntity;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "product_dict")
@Builder
public class ProductDictEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "productDict", fetch = FetchType.LAZY)
    private Set<ProductAliasEntity> aliases = new HashSet<>();

    public void addAlias(ProductAliasEntity productAlias) {
        this.aliases.add(productAlias);
        productAlias.setProductDict(this);
    }
}
