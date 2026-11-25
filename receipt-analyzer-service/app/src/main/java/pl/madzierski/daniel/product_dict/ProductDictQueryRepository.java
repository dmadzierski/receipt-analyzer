package pl.madzierski.daniel.product_dict;

import pl.madzierski.daniel.product_dict.model.ProductDictDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ProductDictQueryRepository {
    List<ProductDictDto> findAllWithCategoryAndAliases();

    Map<UUID, String> findNamesByIds(Collection<UUID> productIds);
}
