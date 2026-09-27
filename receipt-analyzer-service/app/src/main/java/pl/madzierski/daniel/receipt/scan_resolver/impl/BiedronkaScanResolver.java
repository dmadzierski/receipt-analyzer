package pl.madzierski.daniel.receipt.scan_resolver.impl;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;
import pl.madzierski.daniel.receipt.model.ReceiptRevisionResolveData;
import pl.madzierski.daniel.receipt.scan_resolver.ReceiptResolverStrategy;
import pl.madzierski.daniel.receipt.scan_resolver.service.PDFService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.*;
import static pl.madzierski.daniel.exception.AppRuntimeExceptionMessages.OCR_PROCESSING_ERROR;

@AllArgsConstructor
public class BiedronkaScanResolver implements ReceiptResolverStrategy {

    private static final Pattern ITEM_PATTERN_REGEX = Pattern.compile("^(?<name>.*)\\s+(?<ptu>[ABC])\\s+(?<amount>\\d+[\\s.]?\\d+)\\s*[xX]\\s+(?<unitPrice>\\d+[.,\\s]?\\d+)\\s(?<totalPrice>\\d+[.,\\s]\\d+)$");
    private static final Pattern ITEM_PATTERN_WITHOUT_PTU_REGEX = Pattern.compile("^(?<name>.*)\\s+(?<amount>\\d+[\\s.]?\\d+)\\s*[xX]\\s+(?<unitPrice>\\d+[.,\\s]?\\d+)\\s(?<totalPrice>\\d+[.,\\s]\\d+)$");
    private static final Pattern DISCOUNT_PATTERN_REGEX = Pattern.compile("^Rabat [-—]{1,2}(?<discount>\\d+[.,\\s]\\d+)$");
    private static final Pattern DISCOUNTED_PRICE_REGEX = Pattern.compile("^(?<totalPrice>\\d+[.,\\s]\\d+)$");
    private static final Pattern PRICE_SUFFIX_REGEX = Pattern.compile("\\d+[.,\\s]?\\d{2}$");
    private static final Pattern START_ITEM_INDEX_REGEX = Pattern.compile("Nazwa PTU Ilość Cena Wartość", CASE_INSENSITIVE );
    private static final Pattern LAST_ITEM_INDEX_REGEX = Pattern.compile("Sprzeda[zż] opodatkowana C.*", CASE_INSENSITIVE);
    private static final Pattern PAGE_INFO_REGEX = Pattern.compile(".*Strona\\s+\\d+\\s+z\\s+\\d+.*", CASE_INSENSITIVE);
    private static final String WEIGHT_UNIT_PATTER = "(?<=\\d)9(?=\\s|$)";
    private static final String PATTERN_TO_CLEAN_LINE = "^.*?(?=\\d[.,]\\d{3}|[ABC]\\s)";
    private static final String TOTAL_PRICE = "totalPrice";
    private static final String DISCOUNT = "discount";
    private static final String BIEDRONKA_BRAND_NAME = "Biedronka";
    private final String tesseractDataPath;
    private final String resolverVersion;
    private final PDFService pdfService;

    @Override
    public ReceiptResolverStrategyType strategy() {
        return ReceiptResolverStrategyType.BIEDRONKA;
    }


    @Override
    public ReceiptRevisionResolveData execute(List<byte[]> files) {
        if (files == null || files.size() != 1)
            throw new AppRuntimeException(AppRuntimeExceptionMessages.INVALID_INPUT_AMOUNT_OF_INPUT_FILES);

        byte[] file = files.getFirst();
        List<byte[]> imagesData = pdfService.dividePdfFileToImages(file);
        List<String> rawDataList = extractLines(imagesData);

        int startItemsIndex = findFirstItemIndex(rawDataList);

        int lastItemIndex = lastItemIndex(rawDataList);

        if (startItemsIndex == -1 || lastItemIndex == -1 || lastItemIndex < startItemsIndex) 
            throw new AppRuntimeException(AppRuntimeExceptionMessages.CAN_NOT_RESOLVE_RECEIPT);

        List<String> rawItemList = rawDataList.subList(startItemsIndex, lastItemIndex);

        rawItemList = cleanLines(rawItemList);

        List<ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem> items = extractItems(rawItemList);

        BigDecimal totalPrice = items.stream()
            .map(it -> it.totalPrice() != null ? it.totalPrice() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ReceiptRevisionResolveData(resolverVersion, BIEDRONKA_BRAND_NAME, items, null, null,
            strategy(), totalPrice);
    }

    @Nonnull
    private List<ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem> extractItems(List<String> rawItemList) {
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
                    BigDecimal discount = parseBigDecimal(getValueFromGroup(discountMatcher, DISCOUNT));
                    items.set(items.size() - 1, new ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem(lastItem.name(), lastItem.amount(), lastItem.unitPrice(), discount, lastItem.totalPrice(), lastItem.position()));
                }
            } else if (discountedPriceMatcher.matches() && !items.isEmpty()) {
                ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem lastItem = items.getLast();
                BigDecimal totalPrice = parseBigDecimal(getValueFromGroup(discountedPriceMatcher, TOTAL_PRICE));
                items.set(items.size() - 1, new ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem(lastItem.name(), lastItem.amount(), lastItem.unitPrice(), lastItem.discount(), totalPrice, lastItem.position()));
            }
        }

        for (int i = 0; i < items.size(); i++) {
            ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem item = items.get(i);
            items.set(i, new ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem(item.name(), item.amount(), item.unitPrice(), item.discount(), item.totalPrice(), i + 1));
        }
        return items;
    }

    private static List<String> cleanLines(List<String> rawItemList) {
        List<String> mergedItemList = new ArrayList<>();
        String nameBuffer = "";

        for (String line : rawItemList) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) continue;
            if (PAGE_INFO_REGEX.matcher(trimmedLine)
                .matches()) continue;

            if (PRICE_SUFFIX_REGEX.matcher(trimmedLine)
                .find()) {
                if (!nameBuffer.isEmpty()) {
                    String cleanLine = trimmedLine.replaceAll(PATTERN_TO_CLEAN_LINE, "");
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
        return rawItemList;
    }

    private static int lastItemIndex(List<String> rawDataList) {
        int lastItemIndex = -1;
        for (int i = 0; i < rawDataList.size(); i++) {
            if (LAST_ITEM_INDEX_REGEX.matcher(rawDataList.get(i))
                .find()) {
                lastItemIndex = i;
                break;
            }
        }
        return lastItemIndex;
    }

    private static int findFirstItemIndex(List<String> rawDataList) {
        int startItemsIndex = -1;
        for (int i = 0; i < rawDataList.size(); i++) {
            if (START_ITEM_INDEX_REGEX.matcher(rawDataList.get(i)).find()) {
                startItemsIndex = i + 1;
                break;
            }
        }
        return startItemsIndex;
    }

    private List<String> extractLines(List<byte[]> imagesData) {
        List<String> rawDataList = new ArrayList<>();

        for (byte[] imageData : imagesData) {
            String rawData = extractTextFromImage(imageData);
            String[] lines = rawData.split("\n");

            if (lines.length > 0) {
                rawDataList.addAll(Arrays.asList(lines)
                    .subList(0, lines.length - 1));
            }
        }
        return rawDataList;
    }

    private ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem extractItem(int index, Matcher matcher) {
        String name = getValueFromGroup(matcher, "name");
        if (name != null) {
            name = name.replaceAll(WEIGHT_UNIT_PATTER, "g");
        }

        BigDecimal amount = parseBigDecimal(getValueFromGroup(matcher, "amount"));
        BigDecimal unitPrice = parseBigDecimal(getValueFromGroup(matcher, "unitPrice"));
        BigDecimal totalPrice = parseBigDecimal(getValueFromGroup(matcher, "totalPrice"));

        return new ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem(name, amount, unitPrice, null, totalPrice, index);
    }

    private String getValueFromGroup(Matcher matcher, String group) {
        try {
            return matcher.group(group);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String valStr) {
        if (valStr == null) return null;
        try {
            return new BigDecimal(valStr.replace(" ", ".")
                .replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String extractTextFromImage(byte[] imagePath) {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(tesseractDataPath);
        tesseract.setLanguage("pol+eng");
        tesseract.setPageSegMode(6);
        tesseract.setOcrEngineMode(0);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(imagePath)) {
            BufferedImage image = ImageIO.read(bais);
            return tesseract.doOCR(image);
        } catch (TesseractException | IOException e) {
            throw new AppRuntimeException(OCR_PROCESSING_ERROR);
        }
    }
}