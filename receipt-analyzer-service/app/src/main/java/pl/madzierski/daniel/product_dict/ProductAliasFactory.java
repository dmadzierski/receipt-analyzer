package pl.madzierski.daniel.product_dict;

import pl.madzierski.daniel.product_dict.model.ProductAliasDto;

public class ProductAliasFactory {

    ProductAlias from(ProductAliasDto source) {
        if (source == null)
            return null;
        ProductAlias productAlias = new ProductAlias();
        productAlias.setId(source.getId());
        productAlias.setName(source.getName());
        return productAlias;
    }
}
