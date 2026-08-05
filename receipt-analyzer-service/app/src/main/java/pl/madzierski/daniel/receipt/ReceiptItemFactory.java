package pl.madzierski.daniel.receipt;

import lombok.RequiredArgsConstructor;
import pl.madzierski.daniel.product_dict.ProductDictFacade;
import pl.madzierski.daniel.product_dict.model.ProductDictDto;
import pl.madzierski.daniel.product_dict.model.ProductDictQueryEntity;
import pl.madzierski.daniel.receipt.model.ReceiptItemDto;
import pl.madzierski.daniel.receipt.model.ReceiptRevisionResolveData;

import java.util.Optional;

@RequiredArgsConstructor
class ReceiptItemFactory {

    private final ProductDictFacade productDictFacade;

    ReceiptItemEntity from(ReceiptItemDto receiptItemDto) {
        ReceiptItemEntity receiptItemEntity = new ReceiptItemEntity();
        receiptItemEntity.setId(receiptItemDto.getId());
        receiptItemEntity.setName(receiptItemDto.getName());
        receiptItemEntity.setAmount(receiptItemDto.getAmount());
        receiptItemEntity.setUnitPrice(receiptItemDto.getUnitPrice());
        receiptItemEntity.setDiscount(receiptItemDto.getDiscount());
        receiptItemEntity.setTotalPrice(receiptItemDto.getTotalPrice());
        receiptItemEntity.setPosition(receiptItemDto.getPosition());
        if (receiptItemDto.getParentItem() != null)
            receiptItemEntity.setParentItem(from(receiptItemDto.getParentItem()));
        return receiptItemEntity;
    }

    public ReceiptItemEntity from(ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem resolveDataItem) {
        ReceiptItemEntity receiptItemEntity = new ReceiptItemEntity();
        receiptItemEntity.setName(resolveDataItem.name());
        receiptItemEntity.setAmount(resolveDataItem.amount());
        receiptItemEntity.setUnitPrice(resolveDataItem.unitPrice());
        receiptItemEntity.setDiscount(resolveDataItem.discount());
        receiptItemEntity.setTotalPrice(resolveDataItem.totalPrice());
        receiptItemEntity.setPosition(resolveDataItem.position());
        Optional<ProductDictDto> canonicalName = productDictFacade.findCanonicalName(resolveDataItem.name());
        receiptItemEntity.setNameDict(canonicalName.map(productDictDto -> new ProductDictQueryEntity(productDictDto.getId())).orElse(null));
        return receiptItemEntity;
    }
}
