package pl.madzierski.daniel.receipt.scan_resolver.service;

import lombok.AllArgsConstructor;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;
import pl.madzierski.daniel.receipt.model.ReceiptRevisionResolveData;
import pl.madzierski.daniel.receipt.scan_resolver.ReceiptResolverStrategy;

import java.util.List;

import static pl.madzierski.daniel.exception.AppRuntimeExceptionMessages.STRATEGY_NOT_FOUND;

@AllArgsConstructor
public class ReceiptResolverLocatorService {

    private List<ReceiptResolverStrategy> strategyMap;

    public ReceiptRevisionResolveData resolve(List<String> filePaths, ReceiptResolverStrategyType strategyType) {
        return strategyMap.stream()
            .filter((currStrategyType -> currStrategyType.strategy().equals(strategyType)))
            .findFirst()
            .orElseThrow(() -> new AppRuntimeException(STRATEGY_NOT_FOUND)).execute(filePaths);
    }
}