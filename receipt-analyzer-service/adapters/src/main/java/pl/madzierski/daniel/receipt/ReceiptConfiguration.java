package pl.madzierski.daniel.receipt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import pl.madzierski.daniel.file_group.FileFacade;
import pl.madzierski.daniel.file_group.FileQueryRepository;
import pl.madzierski.daniel.product_dict.ProductDictFacade;
import pl.madzierski.daniel.receipt.scan_resolver.ReceiptResolverStrategy;
import pl.madzierski.daniel.receipt.scan_resolver.impl.BiedronkaJsonResolver;
import pl.madzierski.daniel.receipt.scan_resolver.impl.BiedronkaScanResolver;
import pl.madzierski.daniel.receipt.scan_resolver.impl.UserResolver;
import pl.madzierski.daniel.receipt.scan_resolver.service.PDFService;
import pl.madzierski.daniel.receipt.scan_resolver.service.ReceiptResolverLocatorService;

import java.util.List;

@Configuration
class ReceiptConfiguration {
    @Bean
    ReceiptFacade receiptFacade(
        ReceiptItemRepository receiptItemRepository,
        ReceiptItemQueryRepository receiptItemQueryRepository,
        ReceiptRevisionRepository revisionRepository,
        ReceiptRepository receiptRepository,
        FileFacade fileFacade,
        List<ReceiptResolverStrategy> strategyMap,
        ReceiptQueryRepository receiptQueryRepository,
        ReceiptRevisionQueryRepository receiptRevisionQueryRepository,
        FileQueryRepository fileQueryRepository,
        @Lazy ProductDictFacade productDictFacade
    ) {
        ReceiptItemFactory receiptItemFactory = new ReceiptItemFactory(productDictFacade);
        return new ReceiptFacade(
            receiptItemRepository,
            receiptItemQueryRepository,
            receiptItemFactory,
            revisionRepository,
            new ReceiptRevisionFactory(receiptItemFactory),
            receiptRepository,
            fileFacade,
            new ReceiptResolverLocatorService(strategyMap),
            receiptQueryRepository,
            receiptRevisionQueryRepository,
            fileQueryRepository,
            productDictFacade
        );
    }

    @Bean
    BiedronkaJsonResolver biedronkaJsonResolver(@Value("${receipt-resolver-strategy.biedronka.version:1.0}") String resolverVersion, ObjectMapper objectMapper) {
        return new BiedronkaJsonResolver(resolverVersion, objectMapper);
    }

    @Bean
    BiedronkaScanResolver biedronkaScanResolver(@Value("${ocr.tesseract.dataPath}") String tesseractDataPath, @Value("${receipt-resolver-strategy.biedronka.version:1.0}") String resolverVersion, PDFService pdfService) {
        return new BiedronkaScanResolver(tesseractDataPath, resolverVersion, pdfService);
    }

    @Bean
    UserResolver userResolver() {
        return new UserResolver();
    }

    @Bean
    PDFService pdfService(@Value("${pdf-service.tmp-dir}") String tmpFilePath) {
        return new PDFService(tmpFilePath);
    }
}
