package pl.madzierski.daniel.app.receipt.scan_resolver.impl;

import org.springframework.stereotype.Service;
import pl.madzierski.daniel.app.receipt.revision.model.ReceiptRevisionResolveData;
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategy;
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategyType;

import java.util.List;

@Service
public class UserResolver implements ReceiptResolverStrategy {

    @Override
    public ReceiptResolverStrategyType strategy() {
        return ReceiptResolverStrategyType.USER;
    }

    @Override
    public ReceiptRevisionResolveData execute(List<String> filePath) {
        return ReceiptRevisionResolveData.builder().build();
    }
}
