package pl.madzierski.daniel.product_dict;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import pl.madzierski.daniel.receipt.ReceiptFacade;

@Configuration
class ProductDictConfiguration {
    @Bean
    ProductDictFacade productDictFacade(
        ProductDictRepository productDictRepository,
        ProductDictQueryRepository productDictQueryRepository,
        ProductCategoryRepository productCategoryRepository,
        ProductCategoryQueryRepository productCategoryQueryRepository,
        @Lazy ReceiptFacade receiptFacade,
        @Value("${product-dict.min-required-similarity}") Double minRequiredStringSimilarity) {
        return new ProductDictFacade(
            productDictRepository,
            productDictQueryRepository,
            productCategoryRepository,
            productCategoryQueryRepository,
            receiptFacade,
            new ProductMatcher(minRequiredStringSimilarity)
        );
    }
}
