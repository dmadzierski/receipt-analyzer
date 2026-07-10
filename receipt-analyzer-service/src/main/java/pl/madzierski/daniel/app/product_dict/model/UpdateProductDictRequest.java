package pl.madzierski.daniel.app.product_dict.model;

import java.util.List;

public record UpdateProductDictRequest(String canonicalName, List<String> productDictList) {
}
