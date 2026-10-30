package pl.madzierski.daniel.product_dict;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(exclude = {"aliases", "productCategories"})
class ProductDict {
    private UUID id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String name;
    @Getter(AccessLevel.NONE)
    private Set<ProductAlias> aliases = new HashSet<>();
    @Getter(AccessLevel.NONE)
    private Set<ProductCategory> productCategories = new HashSet<>();

    public void addAlias(ProductAlias productAlias) {
        this.aliases.add(productAlias);
    }

    public void addAllAlias(Collection<ProductAlias> aliases) {
        this.aliases.addAll(aliases);
    }

    public Set<ProductAlias> getAliases() {
        return Collections.unmodifiableSet(aliases);
    }

    public Set<ProductCategory> getProductCategories() {
        return Collections.unmodifiableSet(productCategories);
    }

    public void addAllCategories(Collection<ProductCategory> categories) {
        this.productCategories.addAll(categories);
    }
}
