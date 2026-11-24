package pl.madzierski.daniel.product_dict;

import pl.madzierski.daniel.product_dict.model.ProductDictDto;

import java.util.List;

public interface ProductDictQueryRepository {
    List<ProductDictDto> findAllWithCategoryAndAliases();
}
