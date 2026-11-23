package pl.madzierski.daniel.product_dict;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMatcherUT {

    private static final ProductDict ONION = product("Cebula", "Cebula Zolta Luz");
    private static final ProductDict MILK = product("Mleko 3,2%");
    private static final List<ProductDict> PRODUCTS = List.of(ONION, MILK);

    private final ProductMatcher matcher = new ProductMatcher(0.8);

    @Test
    void matchesAliasIgnoringLetterCase() {
        assertThat(matcher.match(PRODUCTS, "CEBULA ZOLTA LUZ", null)).containsSame(ONION);
    }

    @Test
    void matchesUserTranslationWhenAliasIsUnknown() {
        assertThat(matcher.match(PRODUCTS, "CEB ZOL", "mleko 3,2%")).containsSame(MILK);
    }

    @Test
    void matchesSimilarAliasAboveThreshold() {
        assertThat(matcher.match(PRODUCTS, "Cebula Zolta Lu", null)).containsSame(ONION);
    }

    @Test
    void doesNotMatchAliasBelowThreshold() {
        assertThat(matcher.match(PRODUCTS, "Ogorek Szklarniowy", null)).isEmpty();
    }

    @Test
    void picksMostSimilarProduct() {
        ProductDict semiSkimmedMilk = product("Mleko 2%");

        assertThat(matcher.match(List.of(semiSkimmedMilk, MILK), "Mleko 3,2", null)).containsSame(MILK);
    }

    @Test
    void matchesUserTranslationToNameOfProductWithoutThatAlias() {
        ProductDict cheese = ProductDict.restore(null, null, null, null, "Ser", List.of(), List.of());

        assertThat(matcher.match(List.of(cheese, MILK), "Ser Gouda Plastry", "ser")).containsSame(cheese);
    }

    private static ProductDict product(String name, String... aliases) {
        ProductDict product = ProductDict.create(name);
        for (String alias : aliases) {
            product.addAlias(alias);
        }
        return product;
    }
}
