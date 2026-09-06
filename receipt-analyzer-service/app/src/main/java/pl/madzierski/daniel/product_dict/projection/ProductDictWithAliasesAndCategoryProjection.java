package pl.madzierski.daniel.product_dict.projection;

import java.util.Set;

public interface ProductDictWithAliasesAndCategoryProjection {

    String getId();

    String getName();

    Set<ProductCategory> getCategories();

    Set<Alias> getAliases();

    interface ProductCategory {
        String getId();

        String getName();
    }

    interface Alias {
        String getId();

        String getName();
    }
}
