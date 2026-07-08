package pl.madzierski.daniel.app.product_dict;

import jakarta.persistence.Column;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductDictProvider {

    @Value("${product-dict.min-required-similarity}")
    private Double minRequiredStringSimilarity;
    private final ProductDictRepository productDictRepository;

    public Optional<ProductDictEntity> findCanonicalName(String alias) {
        Optional<ProductDictEntity> exactMatch = productDictRepository.findByAlias(alias);
        if (exactMatch.isPresent()) {
            return exactMatch;
        }

        Set<ProductDictEntity> allDictionaries = productDictRepository.findAllCacheable();
        String normalizedSearchAlias = alias.trim().toUpperCase();
        int searchLength = normalizedSearchAlias.length();
        return allDictionaries.parallelStream()
            .flatMap(dict -> dict.getAliases().stream()
                .map(knownAlias -> {
                    String normalizedKnownAlias = knownAlias.trim().toUpperCase();
                    int knownLength = normalizedKnownAlias.length();
                    int maxLength = Math.max(searchLength, knownLength);
                    if (maxLength == 0)
                        return Map.entry(dict, 1.0);
                    int maxAllowedDifference = (int) Math.ceil(maxLength * (1.0 - minRequiredStringSimilarity));
                    double distance = new LevenshteinDistance(maxAllowedDifference).apply(normalizedSearchAlias, normalizedKnownAlias);
                    if (distance == -1) {
                        return Map.entry(dict, 0.0);
                    }
                    double similarityScore = (maxLength - distance) / maxLength;
                    return Map.entry(dict, similarityScore);
                })
            )
            .filter(entry -> entry.getValue() >= minRequiredStringSimilarity)
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey);
    }

    public ProductDictEntity save(ProductDictEntity productDictEntity) {
        return productDictRepository.save(productDictEntity);
    }

    public List<ProductDictEntity> saveAll(Collection<ProductDictEntity> productDictEntities) {
        return productDictRepository.saveAll(productDictEntities);
    }


}
