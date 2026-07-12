package pl.madzierski.daniel.app.product_dict.product_category;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ProductCategoryProvider {

    private final ProductCategoryRepository productCategoryRepository;

    public Optional<ProductCategoryEntity> findProductCategoryById(String productCategoryId) {
        return productCategoryRepository.findById(productCategoryId);
    }
}
