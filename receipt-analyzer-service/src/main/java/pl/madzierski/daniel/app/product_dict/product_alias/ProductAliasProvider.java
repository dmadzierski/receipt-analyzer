package pl.madzierski.daniel.app.product_dict.product_alias;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductAliasProvider {

    private final ProductAliasRepository productAliasRepository;

    public List<ProductAliasEntity> getAliasesByProductDictIdList(List<String> productDictIdList) {
        return productAliasRepository.findAllByProductDictList(productDictIdList);
    }
}