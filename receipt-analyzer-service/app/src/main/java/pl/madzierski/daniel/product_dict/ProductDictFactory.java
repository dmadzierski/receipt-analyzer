package pl.madzierski.daniel.product_dict;

import org.springframework.stereotype.Service;
import pl.madzierski.daniel.product_dict.model.ProductDictDto;

@Service
class ProductDictFactory {
    ProductDictEntity from(ProductDictDto source) {
        ProductDictEntity productDictEntity = new ProductDictEntity();
        productDictEntity.setId(source.getId());
        productDictEntity.setName(source.getName());
        return productDictEntity;
    }
}
