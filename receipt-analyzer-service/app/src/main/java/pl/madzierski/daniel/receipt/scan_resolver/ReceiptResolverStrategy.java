package pl.madzierski.daniel.receipt.scan_resolver;

import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;
import pl.madzierski.daniel.receipt.model.ReceiptRevisionResolveData;

import java.util.List;

public interface ReceiptResolverStrategy {

    ReceiptResolverStrategyType strategy();

    ReceiptRevisionResolveData execute(List<String> filePath);
}
