package pl.madzierski.daniel.receipt;

import lombok.RequiredArgsConstructor;
import pl.madzierski.daniel.receipt.model.ReceiptRevisionDto;
import pl.madzierski.daniel.receipt.model.ReceiptRevisionResolveData;

import java.util.stream.Collectors;

@RequiredArgsConstructor
class ReceiptRevisionFactory {

    private final ReceiptItemFactory receiptItemFactory;

    ReceiptRevisionEntity from(ReceiptRevisionDto receiptRevisionDto) {
        ReceiptRevisionEntity receiptRevisionEntity = new ReceiptRevisionEntity();
        receiptRevisionEntity.setId(receiptRevisionDto.getId());
        receiptRevisionEntity.setName(receiptRevisionDto.getName());
        receiptRevisionEntity.setRevision(receiptRevisionDto.getRevision());
        receiptRevisionEntity.setResolver(receiptRevisionDto.getResolver());
        receiptRevisionEntity.setBrand(receiptRevisionDto.getBrand());
        receiptRevisionEntity.setTotalPrice(receiptRevisionDto.getTotalPrice());
        receiptRevisionEntity.setPayingDate(receiptRevisionDto.getPayingDate());
        receiptRevisionEntity.setAddress(receiptRevisionDto.getAddress());
        receiptRevisionEntity.setIsPreferredRevision(receiptRevisionDto.getIsPreferredRevision());
        receiptRevisionEntity.setIsCorrect(receiptRevisionDto.getIsCorrect());
        return receiptRevisionEntity;
    }

    ReceiptRevisionEntity from(ReceiptRevisionResolveData receiptRevisionDto) {
        ReceiptRevisionEntity receiptRevisionEntity = new ReceiptRevisionEntity();
        receiptRevisionEntity.setRevision(receiptRevisionDto.revisionVersion());
        receiptRevisionEntity.setResolver(receiptRevisionDto.strategy());
        receiptRevisionEntity.setBrand(receiptRevisionDto.brand());
        receiptRevisionEntity.setTotalPrice(receiptRevisionDto.totalPrice());
        receiptRevisionEntity.setPayingDate(receiptRevisionDto.payingDate());
        receiptRevisionEntity.setAddress(receiptRevisionDto.address());
        receiptRevisionEntity.setIsPreferredRevision(false);
        receiptRevisionEntity.setIsCorrect(false);
        receiptRevisionEntity.addItems(receiptRevisionDto.items().stream().map(receiptItemFactory::from)
            .collect(Collectors.toSet()));
        return receiptRevisionEntity;
    }
}
