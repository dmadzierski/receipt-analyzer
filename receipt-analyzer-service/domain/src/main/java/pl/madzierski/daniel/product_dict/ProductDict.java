package pl.madzierski.daniel.product_dict;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pl.madzierski.daniel.exception.AppIllegalStateException;
import pl.madzierski.daniel.exception.AppIllegalStateExceptionMessages;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(exclude = {"aliases", "productCategories"})
class ProductDict {
    private UUID id;
    private Long version;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String name;
    @Getter(AccessLevel.NONE)
    @Builder.Default
    private Set<ProductAlias> aliases = new HashSet<>();
    @Getter(AccessLevel.NONE)
    @Builder.Default
    private Set<ProductCategory> productCategories = new HashSet<>();

    static ProductDict create(String name) {
        ProductDict product = builder()
            .name(requireName(name))
            .build();
        product.addAlias(product.name);
        return product;
    }

    static ProductDict restore(UUID id, Long version, LocalDateTime createdDate, LocalDateTime modifiedDate, String name,
                               Collection<ProductAlias> aliases, Collection<ProductCategory> productCategories) {
        return builder()
            .id(id)
            .version(version)
            .createdDate(createdDate)
            .modifiedDate(modifiedDate)
            .name(name)
            .aliases(new HashSet<>(aliases))
            .productCategories(new HashSet<>(productCategories))
            .build();
    }

    void addAlias(String alias) {
        if (aliases.stream().noneMatch(existing -> existing.getName().equalsIgnoreCase(alias)))
            aliases.add(ProductAlias.create(alias));
    }

    void rename(String name, Set<UUID> keptAliasIds, Collection<ProductDict> mergedProducts) {
        String canonicalName = requireName(name);
        Set<ProductAlias> kept = aliases.stream()
            .filter(alias -> keptAliasIds.contains(alias.getId()))
            .collect(Collectors.toCollection(HashSet::new));
        mergedProducts.stream()
            .flatMap(merged -> merged.aliases.stream())
            .filter(alias -> keptAliasIds.contains(alias.getId()))
            .forEach(alias -> kept.add(ProductAlias.create(alias.getName())));
        if (kept.stream().noneMatch(alias -> alias.getName().equalsIgnoreCase(canonicalName))) {
            kept.add(aliases.stream()
                .filter(alias -> alias.getName().equalsIgnoreCase(canonicalName))
                .findFirst()
                .orElseGet(() -> ProductAlias.create(canonicalName)));
        }
        this.aliases = kept;
        this.name = canonicalName;
    }

    void changeCategories(Collection<ProductCategory> categories) {
        this.productCategories = new HashSet<>(categories);
    }

    Set<ProductAlias> getAliases() {
        return Collections.unmodifiableSet(aliases);
    }

    Set<ProductCategory> getProductCategories() {
        return Collections.unmodifiableSet(productCategories);
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) throw new AppIllegalStateException(AppIllegalStateExceptionMessages.BLANK_NAME);
        return name.trim();
    }
}
