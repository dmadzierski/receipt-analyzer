package pl.madzierski.daniel.digitization.impl;

import lombok.AllArgsConstructor;
import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;
import pl.madzierski.daniel.digitization.model.DigitizedReceipt;
import pl.madzierski.daniel.digitization.ReceiptResolverStrategy;

import java.util.List;

@AllArgsConstructor
public class UserResolver implements ReceiptResolverStrategy {

    @Override
    public ReceiptResolverStrategyType strategy() {
        return ReceiptResolverStrategyType.USER;
    }

    @Override
    public DigitizedReceipt execute(List<byte[]> files) {
        return DigitizedReceipt.builder().strategy(strategy()).build();
    }
}
