package pl.madzierski.daniel.product_dict;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import pl.madzierski.daniel.product_dict.event.ProductDictChanged;
import pl.madzierski.daniel.product_dict.projection.ProductDictWithAliasesAndCategoryProjection;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Document("product_dicts")
record MongoProductDict(@Id UUID id, long version, String name, List<Alias> aliases, List<Category> categories) {

    record Alias(UUID id, String name) {
    }

    record Category(UUID id, String name) {
    }

    static MongoProductDict fromEvent(ProductDictChanged event, Function<UUID, String> categoryName) {
        return new MongoProductDict(event.id(), event.version(), event.name(),
            event.aliases().stream().map(alias -> new Alias(alias.id(), alias.name())).toList(),
            event.categoryIds().stream().map(categoryId -> new Category(categoryId, categoryName.apply(categoryId))).toList());
    }

    ProductDictWithAliasesAndCategoryProjection toProjection() {
        return new ProductDictView(id, version, name,
            categories.stream()
                .filter(category -> category.name() != null)
                .map(category -> new CategoryView(category.id(), category.name()))
                .collect(Collectors.toSet()),
            aliases.stream()
                .map(alias -> new AliasView(alias.id(), alias.name()))
                .collect(Collectors.toSet()));
    }

    private record ProductDictView(UUID getId, long getVersion, String getName,
                                   Set<ProductDictWithAliasesAndCategoryProjection.ProductCategory> getCategories,
                                   Set<ProductDictWithAliasesAndCategoryProjection.Alias> getAliases)
        implements ProductDictWithAliasesAndCategoryProjection {
    }

    private record CategoryView(UUID getId, String getName) implements ProductDictWithAliasesAndCategoryProjection.ProductCategory {
    }

    private record AliasView(UUID getId, String getName) implements ProductDictWithAliasesAndCategoryProjection.Alias {
    }
}
