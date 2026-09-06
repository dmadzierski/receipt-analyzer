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
        ProductAliasRepository productAliasRepository,
        ProductAliasQueryRepository productAliasQueryRepository,
        ProductCategoryRepository productCategoryRepository,
        ProductCategoryFactory productCategoryFactory,
        ProductAliasFactory productAliasFactory,
        @Lazy ReceiptFacade receiptFacade,
        @Value("${product-dict.min-required-similarity}") Double minRequiredStringSimilarity) {
        return new ProductDictFacade(
            productDictRepository,
            productDictQueryRepository,
            productDictFactory(productCategoryFactory, productAliasFactory),
            productAliasRepository,
            productAliasQueryRepository,
            productCategoryRepository,
            receiptFacade,
            minRequiredStringSimilarity
        );
    }

    @Bean
    ProductCategoryFactory productCategoryFactory() {
        return new ProductCategoryFactory();
    }

    @Bean
    ProductAliasFactory productAliasFactory() {
        return new ProductAliasFactory();
    }

    @Bean
    ProductDictFactory productDictFactory(ProductCategoryFactory productCategoryFactory, ProductAliasFactory productAliasFactory) {
        return new ProductDictFactory(productCategoryFactory, productAliasFactory);
    }
}
