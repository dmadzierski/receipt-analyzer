package pl.madzierski.daniel.product_dict;

import pl.madzierski.daniel.product_dict.model.ProductCategoryDto;

import java.util.List;

public interface ProductCategoryQueryRepository {
    List<ProductCategoryDto> findAllOrderedByName();
}
