package pl.madzierski.daniel.app.product_dict.product_category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.madzierski.daniel.app.common.model.BaseEntity;


@Entity
@Table(name = "product_category")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductCategoryEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

}