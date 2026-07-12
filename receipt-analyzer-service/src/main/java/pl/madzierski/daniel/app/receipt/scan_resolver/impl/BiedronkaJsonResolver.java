package pl.madzierski.daniel.app.receipt.scan_resolver.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.madzierski.daniel.app.receipt.revision.model.ReceiptRevisionResolveData;
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategy;
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategyType;
import pl.madzierski.daniel.app.receipt.scan_resolver.model.BiedronkaJsonReceipt;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public final class BiedronkaJsonResolver implements ReceiptResolverStrategy {

    private final String resolverVersion;
    private final ObjectMapper objectMapper;

    @Autowired
    public BiedronkaJsonResolver(@Value("${receipt-resolver-strategy.biedronka.version:1.0}") String resolverVersion, ObjectMapper objectMapper) {
        this.resolverVersion = resolverVersion;
        this.objectMapper = objectMapper;

        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public ReceiptResolverStrategyType strategy() {
        return ReceiptResolverStrategyType.BIEDRONKA_JSON;
    }


    @Override
    public ReceiptRevisionResolveData execute(List<String> filePaths) {
        if (filePaths == null || filePaths.size() != 1) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.INVALID_INPUT_AMOUNT_OF_INPUT_FILES);
        }
        String filePath = filePaths.getFirst();
        File jsonFile = new File(filePath);
        BiedronkaJsonReceipt receipt;
        String rawData;

        try {
            receipt = objectMapper.readValue(jsonFile, BiedronkaJsonReceipt.class);
            rawData = Files.readString(Path.of(filePath));
        } catch (IOException e) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.JSON_PARSING_ERROR);
        }

        List<ReceiptRevisionResolveData.ReceiptRevisionResolveDataFile> files = List.of(new ReceiptRevisionResolveData.ReceiptRevisionResolveDataFile(filePath, 0, rawData));
        List<ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem> items = new ArrayList<>();
        int position = 1;

        if (receipt.body() != null) {
            for (BiedronkaJsonReceipt.Body bodyItem : receipt.body()) {

                if (bodyItem.sellLine() != null) {
                    BiedronkaJsonReceipt.Body.SellLine sellLine = bodyItem.sellLine();
                    if (Boolean.TRUE.equals(sellLine.isStorno())) continue;
                    String name = sellLine.name();
                    Double amount = parseDouble(sellLine.quantity());
                    Double unitPrice = sellLine.price() != null ? sellLine.price() / 100.0 : null;
                    Double totalPrice = sellLine.total() != null ? sellLine.total() / 100.0 : null;

                    items.add(new ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem(name, amount, unitPrice, null, totalPrice, position++));
                } else if (bodyItem.discountLine() != null) {
                    BiedronkaJsonReceipt.Body.DiscountLine discountLine = bodyItem.discountLine();

                    if (Boolean.TRUE.equals(discountLine.isStorno()) || items.isEmpty()) continue;

                    ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem lastItem = items.getLast();

                    Double discount = discountLine.value() != null ? discountLine.value() / 100.0 : 0.0;
                    Double newTotalPrice = lastItem.totalPrice() - discount;

                    items.set(items.size() - 1, new ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem(lastItem.name(), lastItem.amount(), lastItem.unitPrice(), discount, newTotalPrice, lastItem.position()));
                }
            }
        }

        return new ReceiptRevisionResolveData(resolverVersion, "Biedronka", items, files);
    }

    private Double parseDouble(String valStr) {
        if (valStr == null) return null;
        try {
            return Double.parseDouble(valStr.replace(" ", ".").replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}