package pl.madzierski.daniel.receipt.scan_resolver.impl;

import lombok.AllArgsConstructor;
import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;
import pl.madzierski.daniel.receipt.model.ReceiptRevisionResolveData;
import pl.madzierski.daniel.receipt.scan_resolver.ReceiptResolverStrategy;

import java.util.List;

@AllArgsConstructor
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
