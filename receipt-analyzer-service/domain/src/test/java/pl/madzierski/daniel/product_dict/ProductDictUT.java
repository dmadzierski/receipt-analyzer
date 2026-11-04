package pl.madzierski.daniel.product_dict;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

        product.addAliasIfMissing("CEB ZOL");

        assertThat(product.getAliases()).extracting(ProductAlias::getName).containsExactlyInAnyOrder("Cebula", "CEB ZOL");
    }

    @Test
    void doesNotAddAliasDifferingOnlyInLetterCase() {
        ProductDict product = ProductDict.create("Cebula");

        product.addAliasIfMissing("CEBULA");

        assertThat(product.getAliases()).extracting(ProductAlias::getName).containsExactly("Cebula");
    }
}
