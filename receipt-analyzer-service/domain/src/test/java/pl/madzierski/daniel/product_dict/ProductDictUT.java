package pl.madzierski.daniel.product_dict;

import org.junit.jupiter.api.Test;
import pl.madzierski.daniel.exception.AppIllegalStateException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductDictUT {

    @Test
    void createdProductHasItsNameAsAlias() {
        ProductDict product = ProductDict.create("Cebula");

        assertThat(product.getName()).isEqualTo("Cebula");
        assertThat(product.getAliases()).extracting(ProductAlias::getName).containsExactly("Cebula");
    }

    @Test
    void addsMissingAlias() {
        ProductDict product = ProductDict.create("Cebula");

        product.addAlias("CEB ZOL");

        assertThat(product.getAliases()).extracting(ProductAlias::getName).containsExactlyInAnyOrder("Cebula", "CEB ZOL");
    }

    @Test
    void doesNotAddAliasDifferingOnlyInLetterCase() {
        ProductDict product = ProductDict.create("Cebula");

        product.addAlias("CEBULA");

        assertThat(product.getAliases()).extracting(ProductAlias::getName).containsExactly("Cebula");
    }

    @Test
    void productWithoutNameCannotBeCreated() {
        assertThatThrownBy(() -> ProductDict.create("  ")).isInstanceOf(AppIllegalStateException.class);
    }

    @Test
    void renameKeepsChosenAliasesTakesChosenAliasesOfMergedProductsAndAddsCanonicalName() {
        ProductAlias kept = alias("CEB ZOL");
        ProductAlias dropped = alias("CEB typo");
        ProductAlias moved = alias("Cebula zolta luz");
        ProductAlias notMoved = alias("CEB LUZ");
        ProductDict product = product("Cebula", kept, dropped);
        ProductDict merged = product("Cebula żółta", moved, notMoved);

        product.rename("  Cebula żółta ", Set.of(kept.getId(), moved.getId()), List.of(merged));

        assertThat(product.getName()).isEqualTo("Cebula żółta");
        assertThat(product.getAliases()).extracting(ProductAlias::getName)
            .containsExactlyInAnyOrder("CEB ZOL", "Cebula zolta luz", "Cebula żółta");
        assertThat(product.getAliases()).filteredOn(alias -> alias.getName().equals("Cebula zolta luz"))
            .extracting(ProductAlias::getId)
            .containsOnlyNulls();
    }

    @Test
    void renameReusesOwnAliasEqualToNewName() {
        ProductAlias canonical = alias("CEBULA");
        ProductDict product = product("Cebula", canonical);

        product.rename("Cebula", Set.of(), List.of());

        assertThat(product.getAliases()).containsExactly(canonical);
    }

    @Test
    void productCannotBeRenamedToBlankName() {
        ProductDict product = ProductDict.create("Cebula");

        assertThatThrownBy(() -> product.rename(" ", Set.of(), List.of())).isInstanceOf(AppIllegalStateException.class);
        assertThat(product.getName()).isEqualTo("Cebula");
    }

    @Test
    void changeCategoriesReplacesCategories() {
        ProductCategory vegetables = ProductCategory.create("Warzywa");
        ProductCategory organic = ProductCategory.create("Eko");
        ProductDict product = ProductDict.restore(UUID.randomUUID(), 0L, null, null, "Cebula", List.of(), List.of(vegetables));

        product.changeCategories(List.of(organic));

        assertThat(product.getProductCategories()).containsExactly(organic);
    }

    private static ProductDict product(String name, ProductAlias... aliases) {
        return ProductDict.restore(UUID.randomUUID(), 0L, null, null, name, List.of(aliases), List.of());
    }

    private static ProductAlias alias(String name) {
        return ProductAlias.restore(UUID.randomUUID(), null, null, name);
    }
}
