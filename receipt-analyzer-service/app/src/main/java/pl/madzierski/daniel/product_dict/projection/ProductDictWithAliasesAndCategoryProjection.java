package pl.madzierski.daniel.product_dict.projection;

import java.util.Set;
import java.util.UUID;

public interface ProductDictWithAliasesAndCategoryProjection {

    UUID getId();

    String getName();

    Set<ProductCategory> getCategories();

    Set<Alias> getAliases();

    interface ProductCategory {
        UUID getId();

        String getName();
    }

    interface Alias {
        UUID getId();

        String getName();
    }
}
