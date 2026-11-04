package pl.madzierski.daniel.digitization;

import lombok.AllArgsConstructor;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.digitization.model.DigitizedReceipt;
import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;

import java.util.List;

import static pl.madzierski.daniel.exception.AppRuntimeExceptionMessages.STRATEGY_NOT_FOUND;

@AllArgsConstructor
public class DigitizationFacade {

    private final List<ReceiptResolverStrategy> strategies;

    public DigitizedReceipt digitize(List<byte[]> files, ReceiptResolverStrategyType strategyType) {
        return strategies.stream()
            .filter(strategy -> strategy.strategy().equals(strategyType))
            .findFirst()
            .orElseThrow(() -> new AppRuntimeException(STRATEGY_NOT_FOUND))
            .execute(files);
    }
}
