package pl.madzierski.daniel.digitization;

import pl.madzierski.daniel.receipt.ReceiptResolverStrategyType;
import pl.madzierski.daniel.digitization.model.DigitizedReceipt;

import java.util.List;

public interface ReceiptResolverStrategy {

    ReceiptResolverStrategyType strategy();

    DigitizedReceipt execute(List<byte[]> files);
}
