package pl.madzierski.daniel.product_dict;

import org.junit.jupiter.api.Test;
import pl.madzierski.daniel.exception.AppIllegalStateException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductCategoryUT {

    @Test
    void createdCategoryHasTrimmedName() {
        assertThat(ProductCategory.create("  Warzywa ").getName()).isEqualTo("Warzywa");
    }

    @Test
    void categoryWithoutNameCannotBeCreated() {
        assertThatThrownBy(() -> ProductCategory.create(" ")).isInstanceOf(AppIllegalStateException.class);
    }

    @Test
    void renameTrimsNameAndRejectsBlankName() {
        ProductCategory category = ProductCategory.create("Warzywa");

        category.rename(" Owoce ");

        assertThat(category.getName()).isEqualTo("Owoce");
        assertThatThrownBy(() -> category.rename(null)).isInstanceOf(AppIllegalStateException.class);
        assertThat(category.getName()).isEqualTo("Owoce");
    }
}
