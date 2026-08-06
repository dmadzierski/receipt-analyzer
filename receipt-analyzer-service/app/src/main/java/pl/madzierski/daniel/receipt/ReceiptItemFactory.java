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

    ReceiptItem from(ReceiptItemDto receiptItemDto) {
        ReceiptItem receiptItem = new ReceiptItem();
        receiptItem.setId(receiptItemDto.getId());
        receiptItem.setName(receiptItemDto.getName());
        receiptItem.setAmount(receiptItemDto.getAmount());
        receiptItem.setUnitPrice(receiptItemDto.getUnitPrice());
        receiptItem.setDiscount(receiptItemDto.getDiscount());
        receiptItem.setTotalPrice(receiptItemDto.getTotalPrice());
        receiptItem.setPosition(receiptItemDto.getPosition());
        if (receiptItemDto.getParentItem() != null)
            receiptItem.setParentItem(from(receiptItemDto.getParentItem()));
        return receiptItem;
    }

    public ReceiptItem from(ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem resolveDataItem) {
        ReceiptItem receiptItem = new ReceiptItem();
        receiptItem.setName(resolveDataItem.name());
        receiptItem.setAmount(resolveDataItem.amount());
        receiptItem.setUnitPrice(resolveDataItem.unitPrice());
        receiptItem.setDiscount(resolveDataItem.discount());
        receiptItem.setTotalPrice(resolveDataItem.totalPrice());
        receiptItem.setPosition(resolveDataItem.position());
        Optional<ProductDictDto> canonicalName = productDictFacade.findCanonicalName(resolveDataItem.name());
        receiptItem.setNameDict(canonicalName.map(productDictDto -> new ProductDictQueryEntity(productDictDto.getId())).orElse(null));
        return receiptItem;
    }
}
