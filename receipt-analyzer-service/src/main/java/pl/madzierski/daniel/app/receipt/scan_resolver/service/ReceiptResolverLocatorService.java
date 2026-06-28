package pl.madzierski.daniel.app.receipt.scan_resolver.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.madzierski.daniel.app.receipt.revision.model.ReceiptRevisionResolveData;
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategy;
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategyType;
import pl.madzierski.daniel.exception.AppRuntimeException;

import java.util.List;

import static pl.madzierski.daniel.exception.AppRuntimeExceptionMessages.STRATEGY_NOT_FOUND;

@Service
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