package pl.madzierski.daniel.receipt.scan_resolver.impl;

import lombok.AllArgsConstructor;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;
import pl.madzierski.daniel.receipt.model.ReceiptRevisionResolveData;
import pl.madzierski.daniel.receipt.scan_resolver.ReceiptResolverStrategy;
import pl.madzierski.daniel.receipt.scan_resolver.service.PDFService;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pl.madzierski.daniel.exception.AppRuntimeExceptionMessages.OCR_PROCESSING_ERROR;

@AllArgsConstructor
public class BiedronkaScanResolver implements ReceiptResolverStrategy {

    private static final Pattern ITEM_PATTERN_REGEX = Pattern.compile("^(?<name>.*)\\s+(?<ptu>[ABC])\\s+(?<amount>\\d+[\\s.]?\\d+)\\s*[xX]\\s+(?<unitPrice>\\d+[.,\\s]?\\d+)\\s(?<totalPrice>\\d+[.,\\s]\\d+)$");
    private static final Pattern ITEM_PATTERN_WITHOUT_PTU_REGEX = Pattern.compile("^(?<name>.*)\\s+(?<amount>\\d+[\\s.]?\\d+)\\s*[xX]\\s+(?<unitPrice>\\d+[.,\\s]?\\d+)\\s(?<totalPrice>\\d+[.,\\s]\\d+)$");
    private static final Pattern DISCOUNT_PATTERN_REGEX = Pattern.compile("^Rabat -(?<discount>\\d+[.,\\s]\\d+)$");
    private static final Pattern DISCOUNTED_PRICE_REGEX = Pattern.compile("^(?<totalPrice>\\d+[.,\\s]\\d+)$");
    private static final Pattern PRICE_SUFFIX_REGEX = Pattern.compile("\\d+[.,\\s]?\\d{2}$");
    private static final Pattern START_ITEM_INDEX_REGEX = Pattern.compile("Nazwa PTU Ilość Cena Wartość", Pattern.CASE_INSENSITIVE);
    private static final Pattern LAST_ITEM_INDEX_REGEX = Pattern.compile("Sprzeda[zż] opodatkowana C.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern PAGE_INFO_REGEX = Pattern.compile(".*Strona\\s+\\d+\\s+z\\s+\\d+.*", Pattern.CASE_INSENSITIVE);
    private final String tesseractDataPath;
    private final String resolverVersion;
    private final PDFService pdfService;

    @Override
    public ReceiptResolverStrategyType strategy() {
        return ReceiptResolverStrategyType.BIEDRONKA;
    }


    @Override
    public ReceiptRevisionResolveData execute(List<String> filePaths) {
        if (filePaths == null || filePaths.size() != 1) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.INVALID_INPUT_AMOUNT_OF_INPUT_FILES);
        }
        String filePath = filePaths.getFirst();
        List<String> pdfPaths = pdfService.dividePdfFileToImages(filePath);
        List<ReceiptRevisionResolveData.ReceiptRevisionResolveDataFile> receiptFileList = new ArrayList<>();
        List<String> rawDataList = new ArrayList<>();

        for (int i = 0; i < pdfPaths.size(); i++) {
            String pdfPath = pdfPaths.get(i);
            String rawData = extractTextFromImage(pdfPath);
            receiptFileList.add(new ReceiptRevisionResolveData.ReceiptRevisionResolveDataFile(pdfPath, i, rawData));

            String[] lines = rawData.split("\n");
            rawDataList.addAll(Arrays.asList(lines).subList(0, lines.length - 1));
        }

        int startItemsIndex = -1;
        for (int i = 0; i < rawDataList.size(); i++) {
            if (START_ITEM_INDEX_REGEX.matcher(rawDataList.get(i)).find()) {
                startItemsIndex = i + 1;
                break;
            }
        }

        int lastItemIndex = -1;
        for (int i = 0; i < rawDataList.size(); i++) {
            if (LAST_ITEM_INDEX_REGEX.matcher(rawDataList.get(i)).find()) {
                lastItemIndex = i;
                break;
            }
        }

        if (startItemsIndex == -1 || lastItemIndex == -1 || lastItemIndex < startItemsIndex) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.CAN_NOT_RESOLVE_RECEIPT);
        }

        List<String> rawItemList = rawDataList.subList(startItemsIndex, lastItemIndex);
        List<String> mergedItemList = new ArrayList<>();
        String nameBuffer = "";

        for (String line : rawItemList) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) continue;
            if (PAGE_INFO_REGEX.matcher(trimmedLine).matches()) continue;

            if (PRICE_SUFFIX_REGEX.matcher(trimmedLine).find()) {
                if (!nameBuffer.isEmpty()) {
                    String cleanLine = trimmedLine.replaceAll("^.*?(?=\\d[.,]\\d{3}|[ABC]\\s)", "");
                    mergedItemList.add(nameBuffer + " " + cleanLine);
                    nameBuffer = "";
                } else {
                    mergedItemList.add(trimmedLine);
                }
            } else {
                nameBuffer = trimmedLine;
            }
        }

        rawItemList = mergedItemList;
        List<ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem> items = new ArrayList<>();

        for (String line : rawItemList) {
            Matcher itemMatcher = ITEM_PATTERN_REGEX.matcher(line);
            Matcher itemWithoutPtuMatcher = ITEM_PATTERN_WITHOUT_PTU_REGEX.matcher(line);
            Matcher discountMatcher = DISCOUNT_PATTERN_REGEX.matcher(line);
            Matcher discountedPriceMatcher = DISCOUNTED_PRICE_REGEX.matcher(line);

            if (itemMatcher.matches()) {
                items.add(extractItem(items.size() + 1, itemMatcher));
            } else if (itemWithoutPtuMatcher.matches()) {
                items.add(extractItem(items.size() + 1, itemWithoutPtuMatcher));
            } else if (discountMatcher.matches()) {
                if (!items.isEmpty()) {
                    ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem lastItem = items.getLast();
                    Double discount = parseDouble(getValueFromGroup(discountMatcher, "discount"));
                    items.set(items.size() - 1, new ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem(lastItem.name(), lastItem.amount(), lastItem.unitPrice(), discount, lastItem.totalPrice(), lastItem.position()));
                }
            } else if (discountedPriceMatcher.matches() && !items.isEmpty()) {
                ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem lastItem = items.getLast();
                Double totalPrice = parseDouble(getValueFromGroup(discountedPriceMatcher, "totalPrice"));
                items.set(items.size() - 1, new ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem(lastItem.name(), lastItem.amount(), lastItem.unitPrice(), lastItem.discount(), totalPrice, lastItem.position()));
            }
        }

        for (int i = 0; i < items.size(); i++) {
            ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem item = items.get(i);
            items.set(i, new ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem(item.name(), item.amount(), item.unitPrice(), item.discount(), item.totalPrice(), i + 1));
        }

        double totalPrice = items.stream()
            .mapToDouble(it -> it.totalPrice() != null ? it.totalPrice() : 0.0)
            .sum();

        return new ReceiptRevisionResolveData(resolverVersion, "Biedronka", items, receiptFileList, null, null,
            strategy(), totalPrice);
    }

    private ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem extractItem(int index, Matcher matcher) {
        String name = getValueFromGroup(matcher, "name");
        if (name != null) {
            name = name.replaceAll("(?<=\\d)9(?=\\s|$)", "g");
        }

        Double amount = parseDouble(getValueFromGroup(matcher, "amount"));
        Double unitPrice = parseDouble(getValueFromGroup(matcher, "unitPrice"));
        Double totalPrice = parseDouble(getValueFromGroup(matcher, "totalPrice"));

        return new ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem(name, amount, unitPrice, null, totalPrice, index);
    }

    private String getValueFromGroup(Matcher matcher, String group) {
        try {
            return matcher.group(group);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Double parseDouble(String valStr) {
        if (valStr == null) return null;
        try {
            return Double.parseDouble(valStr.replace(" ", ".").replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String extractTextFromImage(String imagePath) {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(tesseractDataPath);
        tesseract.setLanguage("pol+eng");
        tesseract.setPageSegMode(6);
        tesseract.setOcrEngineMode(0);

        try {
            return tesseract.doOCR(new File(imagePath));
        } catch (TesseractException e) {
            throw new AppRuntimeException(OCR_PROCESSING_ERROR);
        }
    }
}