package pl.madzierski.daniel.receipt.scan_resolver.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.madzierski.daniel.IntegrationTest;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;
import pl.madzierski.daniel.receipt.model.ReceiptRevisionResolveData;
import pl.madzierski.daniel.receipt.model.ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class BiedronkaJsonResolverIT extends IntegrationTest {

    private static final String RECEIPT = "receipts/biedronka/biedronka_e_receipt.json";

    @Autowired
    private BiedronkaJsonResolver resolver;

    @Test
    void readsItemsInOrderWithWeighedItemDetails() {
        ReceiptRevisionResolveData result = resolver.execute(List.of(readResource(RECEIPT)));

        assertThat(result.strategy()).isEqualTo(ReceiptResolverStrategyType.BIEDRONKA_JSON);
        assertThat(result.brand()).isEqualTo("Biedronka");
        assertThat(result.items()).extracting(ReceiptRevisionResolveDataItem::position)
            .containsExactlyElementsOf(IntStream.rangeClosed(1, 36).boxed().toList());
        ReceiptRevisionResolveDataItem onion = result.items().getFirst();
        assertThat(onion.name()).isEqualTo("Cebula Żółta Luz");
        assertThat(onion.amount()).isEqualByComparingTo("0.542");
        assertThat(onion.unitPrice()).isEqualByComparingTo("3.49");
        assertThat(onion.totalPrice()).isEqualByComparingTo("1.89");
    }

    @Test
    void rejectsFileThatIsNotJson() {
        byte[] notJson = "not a receipt".getBytes(StandardCharsets.UTF_8);

        assertError(() -> resolver.execute(List.of(notJson)), AppRuntimeExceptionMessages.JSON_PARSING_ERROR);
    }

    @Test
    void acceptsExactlyOneFile() {
        byte[] receipt = readResource(RECEIPT);

        assertError(() -> resolver.execute(List.of(receipt, receipt)), AppRuntimeExceptionMessages.INVALID_INPUT_AMOUNT_OF_INPUT_FILES);
    }
}
