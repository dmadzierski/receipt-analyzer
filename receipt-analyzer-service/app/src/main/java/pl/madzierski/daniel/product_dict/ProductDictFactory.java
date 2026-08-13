package pl.madzierski.daniel.product_dict;

import pl.madzierski.daniel.product_dict.model.ProductDto;

class ProductDictFactory {
    ProductDict from(ProductDto source) {
        ProductDict productDict = new ProductDict();
        productDict.setId(source.getId());
        productDict.setName(source.getName());
        return productDict;
    }
}
