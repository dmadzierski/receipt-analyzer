package pl.madzierski.daniel.app.product_dict.product_alias;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.madzierski.daniel.app.common.model.BaseEntity;
import pl.madzierski.daniel.app.product_dict.ProductDictEntity;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "product_alias")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductAliasEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_dict_id", nullable = false)
    private ProductDictEntity productDict;

    public ProductAliasEntity(String name) {
        this.name = name;
    }
}