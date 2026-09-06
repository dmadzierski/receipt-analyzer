package pl.madzierski.daniel.product_dict;

import pl.madzierski.daniel.product_dict.model.ProductCategoryDto;

public class ProductCategoryFactory {
    ProductCategory from(ProductCategoryDto source) {
        if (source == null)
            return null;
        ProductCategory productCategory = new ProductCategory();
        productCategory.setId(source.getId());
        productCategory.setName(source.getName());
        return productCategory;
    }
}
