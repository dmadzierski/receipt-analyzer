package pl.madzierski.daniel.app.receipt.scan_resolver;

import pl.madzierski.daniel.app.receipt.revision.model.ReceiptRevisionResolveData;

import java.util.List;

public interface ReceiptResolverStrategy {

    ReceiptResolverStrategyType strategy();

    ReceiptRevisionResolveData execute(List<String> filePath);
}
