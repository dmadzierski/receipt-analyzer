package pl.madzierski.daniel.product_dict;

import lombok.AllArgsConstructor;
import pl.madzierski.daniel.product_dict.model.ProductDto;

import java.util.stream.Collectors;

@AllArgsConstructor
class ProductDictFactory {

    private final ProductCategoryFactory productCategoryFactory;
    private final ProductAliasFactory productAliasFactory;

    ProductDict from(ProductDto source) {
        if (source == null)
            return null;
        ProductDict productDict = new ProductDict();
        productDict.setId(source.getId());
        productDict.setName(source.getName());
        productDict.setAliases(source.getAliases().stream().map(productAliasFactory::from).collect(Collectors.toSet()));
        productDict.setProductCategories(source.getProductCategories().stream().map(productCategoryFactory::from).collect(Collectors.toSet()));
        return productDict;
    }
}
