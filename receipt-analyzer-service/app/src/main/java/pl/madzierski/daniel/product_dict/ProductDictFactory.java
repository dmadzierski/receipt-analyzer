package pl.madzierski.daniel.product_dict;

import pl.madzierski.daniel.product_dict.model.ProductDictDto;

class ProductDictFactory {
    ProductDict from(ProductDictDto source) {
        ProductDict productDict = new ProductDict();
        productDict.setId(source.getId());
        productDict.setName(source.getName());
        return productDict;
    }
}
