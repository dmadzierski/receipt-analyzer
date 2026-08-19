package pl.madzierski.daniel.receipt;

import lombok.RequiredArgsConstructor;
import pl.madzierski.daniel.receipt.model.ReceiptRevisionDto;
import pl.madzierski.daniel.receipt.model.ReceiptRevisionResolveData;

import java.util.stream.Collectors;

@RequiredArgsConstructor
class ReceiptRevisionFactory {

    private final ReceiptItemFactory receiptItemFactory;

    ReceiptRevision from(ReceiptRevisionDto receiptRevisionDto) {
        ReceiptRevision receiptRevision = new ReceiptRevision();
        receiptRevision.setId(receiptRevisionDto.getId());
        receiptRevision.setName(receiptRevisionDto.getName());
        receiptRevision.setRevision(receiptRevisionDto.getRevision());
        receiptRevision.setResolver(receiptRevisionDto.getResolver());
        receiptRevision.setTotalPrice(receiptRevisionDto.getTotalPrice());
        receiptRevision.setPaymentDate(receiptRevisionDto.getPaymentDate());
        receiptRevision.setIsPreferredRevision(receiptRevisionDto.getIsPreferredRevision());
        receiptRevision.setIsCorrect(receiptRevisionDto.getIsCorrect());
        return receiptRevision;
    }

    ReceiptRevision from(ReceiptRevisionResolveData receiptRevisionDto) {
        ReceiptRevision receiptRevision = new ReceiptRevision();
        receiptRevision.setRevision(receiptRevisionDto.revisionVersion());
        receiptRevision.setResolver(receiptRevisionDto.strategy());
        receiptRevision.setBrand(receiptRevisionDto.brand());
        receiptRevision.setTotalPrice(receiptRevisionDto.totalPrice());
        receiptRevision.setPaymentDate(receiptRevisionDto.paymentDate());
        receiptRevision.setAddress(receiptRevisionDto.address());
        receiptRevision.setIsPreferredRevision(false);
        receiptRevision.setIsCorrect(false);
        receiptRevision.addItems(receiptRevisionDto.items().stream().map(receiptItemFactory::from)
            .collect(Collectors.toSet()));
        return receiptRevision;
    }
}
