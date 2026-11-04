package pl.madzierski.daniel.receipt;

import pl.madzierski.daniel.product_dict.model.ProductDictQuery;

import java.math.BigDecimal;
import java.util.UUID;

record ReceiptItemChange(UUID id, String name, ProductDictQuery nameDict, BigDecimal amount, BigDecimal unitPrice,
                         BigDecimal discount, BigDecimal totalPrice, Integer position) {
}
