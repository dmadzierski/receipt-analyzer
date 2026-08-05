package pl.madzierski.daniel.receipt.scan_resolver.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;
import pl.madzierski.daniel.receipt.model.ReceiptRevisionResolveData;
import pl.madzierski.daniel.receipt.model.ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem;
import pl.madzierski.daniel.receipt.scan_resolver.ReceiptResolverStrategy;
import pl.madzierski.daniel.receipt.scan_resolver.model.BiedronkaJsonReceipt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class BiedronkaJsonResolver implements ReceiptResolverStrategy {

    public static final String BRAND = "Biedronka";
    private final String resolverVersion;
    private final ObjectMapper objectMapper;

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
        Result result = new Result(receipt, rawData);

        List<ReceiptRevisionResolveData.ReceiptRevisionResolveDataFile> files = List.of(new ReceiptRevisionResolveData.ReceiptRevisionResolveDataFile(filePath, 0, result.rawData()));
        List<ReceiptRevisionResolveDataItem> items = new ArrayList<>();
        int position = 1;

        if (result.receipt().body() == null)
            return new ReceiptRevisionResolveData(resolverVersion, BRAND, items, files, null, null, this.strategy(),
                null);

        for (BiedronkaJsonReceipt.Body bodyItem : result.receipt().body()) {
            if ((bodyItem.sellLine() != null && Boolean.TRUE.equals(bodyItem.sellLine().isStorno())) || bodyItem.discountLine() != null && Boolean.TRUE.equals(bodyItem.discountLine().isStorno()))
                continue;
            if (bodyItem.sellLine() != null) {
                BiedronkaJsonReceipt.Body.SellLine sellLine = bodyItem.sellLine();
                ReceiptRevisionResolveDataItem receiptRevisionResolveDataItem = getReceiptRevisionResolveDataItem(sellLine, position++);
                items.add(receiptRevisionResolveDataItem);
            } else if (bodyItem.discountLine() != null) {
                BiedronkaJsonReceipt.Body.DiscountLine discountLine = bodyItem.discountLine();
                ReceiptRevisionResolveDataItem receiptRevisionResolveDataItem = getReceiptRevisionResolveDataItem(items.getLast(), discountLine);
                items.set(items.size() - 1, receiptRevisionResolveDataItem);
            }
        }

        Double totalPrice =
            result.receipt().body().stream().filter(b -> b.sumInCurrency() != null).findFirst().map(b -> b.sumInCurrency().fiscalTotal()).orElse(0) / 100.0;
        return new ReceiptRevisionResolveData(resolverVersion, BRAND, items, files, null, null, this.strategy(),
            totalPrice);
    }

    private ReceiptRevisionResolveDataItem getReceiptRevisionResolveDataItem(BiedronkaJsonReceipt.Body.SellLine sellLine, Integer position) {
        String name = sellLine.name();
        Double amount = parseDouble(sellLine.quantity());
        Double unitPrice = sellLine.price() != null ? sellLine.price() / 100.0 : null;
        Double totalPrice = sellLine.total() != null ? sellLine.total() / 100.0 : null;
        return new ReceiptRevisionResolveDataItem(name, amount, unitPrice, null, totalPrice, position);
    }

    private ReceiptRevisionResolveDataItem getReceiptRevisionResolveDataItem(ReceiptRevisionResolveDataItem lastItem, BiedronkaJsonReceipt.Body.DiscountLine discountLine) {
        Double discount = discountLine.value() != null ? discountLine.value() / 100.0 : 0.0;
        Double newTotalPrice = lastItem.totalPrice() - discount;
        return new ReceiptRevisionResolveDataItem(lastItem.name(), lastItem.amount(), lastItem.unitPrice(), discount, newTotalPrice, lastItem.position());
    }

    private Double parseDouble(String valStr) {
        if (valStr == null) return null;
        try {
            return Double.parseDouble(valStr.replace(" ", ".").replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private record Result(BiedronkaJsonReceipt receipt, String rawData) {
    }
}