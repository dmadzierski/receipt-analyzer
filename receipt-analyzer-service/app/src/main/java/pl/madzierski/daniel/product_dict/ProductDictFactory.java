package pl.madzierski.daniel.product_dict;

import pl.madzierski.daniel.product_dict.model.ProductDictDto;

class ProductDictFactory {
    ProductDictEntity from(ProductDictDto source) {
        ProductDictEntity productDictEntity = new ProductDictEntity();
        productDictEntity.setId(source.getId());
        productDictEntity.setName(source.getName());
        return productDictEntity;
    }
}
