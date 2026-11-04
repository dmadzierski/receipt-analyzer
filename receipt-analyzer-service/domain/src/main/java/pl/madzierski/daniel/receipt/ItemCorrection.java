package pl.madzierski.daniel.receipt;

import pl.madzierski.daniel.product_dict.model.ProductDictQuery;

import java.util.UUID;

record ItemCorrection(UUID originalItemId, String originalName, String correctedName, ProductDictQuery chosenProduct) {
}
