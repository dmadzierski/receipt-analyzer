package pl.madzierski.daniel.product_dict;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ProductDictConfiguration {
    @Bean
    ProductDictFacade productDictFacade(
        ProductDictRepository productDictRepository,
        ProductDictQueryRepository productDictQueryRepository,
        ProductCategoryRepository productCategoryRepository,
        ProductCategoryQueryRepository productCategoryQueryRepository,
        MergedProductReassignment mergedProductReassignment,
        @Value("${product-dict.min-required-similarity}") Double minRequiredStringSimilarity) {
        return new ProductDictFacade(
            productDictRepository,
            productDictQueryRepository,
            productCategoryRepository,
            productCategoryQueryRepository,
            mergedProductReassignment,
            new ProductMatcher(minRequiredStringSimilarity)
        );
    }
}
