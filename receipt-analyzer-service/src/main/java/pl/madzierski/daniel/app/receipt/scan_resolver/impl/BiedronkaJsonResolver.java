package pl.madzierski.daniel.app.receipt.scan_resolver.impl;

import org.springframework.stereotype.Service;
import pl.madzierski.daniel.app.receipt.revision.model.ReceiptRevisionResolveData;
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategy;
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategyType;

import java.util.List;

@Service
public final class BiedronkaJsonResolver implements ReceiptResolverStrategy {

    public ReceiptResolverStrategyType strategy() {
        return ReceiptResolverStrategyType.BIEDRONKA_JSON;
    }


    public ReceiptRevisionResolveData execute(List<String> filePaths) {
        return null;
    }
}