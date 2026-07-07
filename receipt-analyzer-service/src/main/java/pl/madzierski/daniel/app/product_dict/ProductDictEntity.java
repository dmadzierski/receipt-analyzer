package pl.madzierski.daniel.app.product_dict;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.app.common.model.BaseEntity;

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

    @ElementCollection
    @CollectionTable(
            name = "product_aliases",
            joinColumns = @JoinColumn(name = "dictionary_id")
    )
    @Column(name = "alias")
    private Set<String> aliases = new HashSet<>();

    public void addAlias(String alias) {
        this.aliases.add(alias);
    }
}
