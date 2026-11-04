package pl.madzierski.daniel.receipt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import pl.madzierski.daniel.file_group.FileFacade;
import pl.madzierski.daniel.file_group.FileGroupQueryRepository;
import pl.madzierski.daniel.digitization.DigitizationFacade;
import pl.madzierski.daniel.product_dict.ProductDictFacade;
import pl.madzierski.daniel.product_dict.ProductDictQueryRepository;
import pl.madzierski.daniel.store.StoreQueryRepository;
import pl.madzierski.daniel.wallet.WalletQueryRepository;

@Configuration
class ReceiptConfiguration {
    @Bean
    ReceiptFacade receiptFacade(
        ReceiptItemRepository receiptItemRepository,
        ReceiptItemQueryRepository receiptItemQueryRepository,
        ReceiptRevisionRepository revisionRepository,
        ReceiptRepository receiptRepository,
        FileFacade fileFacade,
        DigitizationFacade digitizationFacade,
        ReceiptQueryRepository receiptQueryRepository,
        ReceiptRevisionQueryRepository receiptRevisionQueryRepository,
        StoreQueryRepository storeQueryRepository,
        @Lazy ProductDictFacade productDictFacade,
        FileGroupQueryRepository fileGroupQueryRepository,
        ProductDictQueryRepository productDictQueryRepository,
        WalletQueryRepository walletQueryRepository
    ) {
        return new ReceiptFacade(
            receiptItemRepository,
            receiptItemQueryRepository,
            revisionRepository,
            new ReceiptFactory(),
            new ReceiptRevisionFactory(),
            receiptRepository,
            fileFacade,
            digitizationFacade,
            receiptQueryRepository,
            receiptRevisionQueryRepository,
            storeQueryRepository,
            productDictFacade,
            productDictQueryRepository,
            fileGroupQueryRepository,
            walletQueryRepository
        );
    }
}
