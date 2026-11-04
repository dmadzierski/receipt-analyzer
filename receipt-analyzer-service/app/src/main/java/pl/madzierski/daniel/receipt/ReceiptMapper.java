package pl.madzierski.daniel.receipt;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.madzierski.daniel.product_dict.model.ProductDictQuery;
import pl.madzierski.daniel.receipt.model.UpdateRevisionRequest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ReceiptMapper {

    static ReceiptItemChange toItemChange(UpdateRevisionRequest.ItemRequest request) {
        return new ReceiptItemChange(request.id(), request.name(),
            request.productId() != null ? new ProductDictQuery(request.productId()) : null,
            request.amount(), request.unitPrice(), request.discount(), request.totalPrice(), request.position());
    }
}
