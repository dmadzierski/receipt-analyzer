package pl.madzierski.daniel.receipt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.madzierski.daniel.file_group.FileFacade;
import pl.madzierski.daniel.file_group.FileGroupQueryRepository;
import pl.madzierski.daniel.digitization.DigitizationFacade;
import pl.madzierski.daniel.product_dict.ProductDictFacade;
import pl.madzierski.daniel.store.StoreQueryRepository;
import pl.madzierski.daniel.wallet.WalletQueryRepository;

@Configuration
class ReceiptConfiguration {
    @Bean
    ReceiptFacade receiptFacade(
        ReceiptItemQueryRepository receiptItemQueryRepository,
        ReceiptRevisionRepository revisionRepository,
        ReceiptRepository receiptRepository,
        FileFacade fileFacade,
        DigitizationFacade digitizationFacade,
        ReceiptQueryRepository receiptQueryRepository,
        ReceiptRevisionQueryRepository receiptRevisionQueryRepository,
        StoreQueryRepository storeQueryRepository,
        ProductDictFacade productDictFacade,
        FileGroupQueryRepository fileGroupQueryRepository,
        WalletQueryRepository walletQueryRepository
    ) {
        return new ReceiptFacade(
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
            fileGroupQueryRepository,
            walletQueryRepository
        );
    }

    @Bean
    ReceiptMergedProductReassignment receiptMergedProductReassignment(ReceiptRevisionRepository revisionRepository) {
        return new ReceiptMergedProductReassignment(revisionRepository);
    }
}
