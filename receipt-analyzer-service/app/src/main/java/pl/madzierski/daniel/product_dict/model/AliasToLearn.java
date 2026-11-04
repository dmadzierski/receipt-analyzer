package pl.madzierski.daniel.product_dict.model;

import java.util.UUID;

public record AliasToLearn(String alias, String productName, UUID productId) {
}
