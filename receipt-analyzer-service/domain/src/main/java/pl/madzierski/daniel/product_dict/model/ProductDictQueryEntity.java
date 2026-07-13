package pl.madzierski.daniel.product_dict.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_dict")
@AllArgsConstructor
@NoArgsConstructor
public class ProductDictQueryEntity {

    @Id
    private String id;
}
