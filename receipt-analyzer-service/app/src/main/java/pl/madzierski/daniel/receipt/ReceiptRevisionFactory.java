package pl.madzierski.daniel.receipt;

import pl.madzierski.daniel.digitization.model.DigitizedReceipt;
import pl.madzierski.daniel.file_group.model.FileGroupQuery;

import java.util.List;

class ReceiptRevisionFactory {

    ReceiptRevision from(DigitizedReceipt digitizedReceipt, Receipt receipt, FileGroupQuery sourceFileGroup) {
        List<ReceiptItem> items = digitizedReceipt.items() == null ? List.of() : digitizedReceipt.items()
            .stream()
            .map(item -> ReceiptItem.create(item.name(), null, item.amount(), item.unitPrice(), item.discount(),
                item.totalPrice(), item.position()))
            .toList();
        return ReceiptRevision.createDigitized(digitizedReceipt.revisionVersion(), digitizedReceipt.strategy(),
            digitizedReceipt.totalPrice(), digitizedReceipt.paymentDate(), items, receipt, sourceFileGroup);
    }
}
