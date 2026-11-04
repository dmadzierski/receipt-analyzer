package pl.madzierski.daniel.product_dict;

import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
class ProductMatcher {

    private final double minRequiredSimilarity;

    Optional<ProductDict> match(Collection<ProductDict> products, String alias, String userTranslation) {
        Optional<ProductDict> exactMatch = products.stream()
            .filter(product -> product.getAliases().stream()
                .map(ProductAlias::getName)
                .anyMatch(known -> known.equalsIgnoreCase(alias) || known.equalsIgnoreCase(userTranslation)))
            .findAny();
        if (exactMatch.isPresent()) return exactMatch;
        String normalizedSearchAlias = alias.trim().toUpperCase();
        return products.parallelStream()
            .flatMap(product -> product.getAliases().stream()
                .map(known -> Map.entry(product, similarity(normalizedSearchAlias, known.getName().trim().toUpperCase()))))
            .filter(entry -> entry.getValue() >= minRequiredSimilarity)
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey);
    }

    private double similarity(String searched, String known) {
        int maxLength = Math.max(searched.length(), known.length());
        if (maxLength == 0) return 1.0;
        int maxAllowedDifference = (int) Math.ceil(maxLength * (1.0 - minRequiredSimilarity));
        int distance = new LevenshteinDistance(maxAllowedDifference).apply(searched, known);
        if (distance == -1) return 0.0;
        return (double) (maxLength - distance) / maxLength;
    }
}
