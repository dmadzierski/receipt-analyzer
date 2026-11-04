package pl.madzierski.daniel.digitization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.madzierski.daniel.digitization.impl.BiedronkaJsonResolver;
import pl.madzierski.daniel.digitization.impl.BiedronkaScanResolver;
import pl.madzierski.daniel.digitization.impl.UserResolver;

import java.util.List;

@Configuration
class DigitizationConfiguration {

    @Bean
    DigitizationFacade digitizationFacade(List<ReceiptResolverStrategy> strategies) {
        return new DigitizationFacade(strategies);
    }

    @Bean
    BiedronkaJsonResolver biedronkaJsonResolver(@Value("${receipt-resolver-strategy.biedronka-json.version:1.0}") String resolverVersion, ObjectMapper objectMapper) {
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
    PDFService pdfService() {
        return new PDFService();
    }
}
