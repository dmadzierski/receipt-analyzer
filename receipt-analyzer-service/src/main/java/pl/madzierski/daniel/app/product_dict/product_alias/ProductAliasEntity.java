package pl.madzierski.daniel.app.product_dict.product_alias;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.madzierski.daniel.app.common.model.BaseEntity;
import pl.madzierski.daniel.app.product_dict.ProductDictEntity;

import java.util.Objects;


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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ProductAliasEntity that = (ProductAliasEntity) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(name);
        return result;
    }
}