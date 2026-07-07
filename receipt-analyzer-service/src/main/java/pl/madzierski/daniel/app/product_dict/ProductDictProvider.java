package pl.madzierski.daniel.app.product_dict;

import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductDictProvider {

    @Value("${product-dict.max-allowed-distance:2}")
    private int maxAllowedDamerauLevenshteinDistance;
    @Value("${product-dict.min-required-similarity:0.8}")
    private Double minRequiredStringSimilarity;
    private final ProductDictRepository productDictRepository;

    public Optional<ProductDictEntity> findCanonicalName(String alias) {
        Optional<ProductDictEntity> exactMatch = productDictRepository.findByAlias(alias);
        if (exactMatch.isPresent()) {
            return exactMatch;
        }

        List<ProductDictEntity> allDictionaries = productDictRepository.findAll();
        String normalizedSearchAlias = alias.trim().toUpperCase();
        LevenshteinDistance levenshtein = new LevenshteinDistance(maxAllowedDamerauLevenshteinDistance);
        return allDictionaries.parallelStream().flatMap(dict -> dict.getAliases().stream().map(knownAlias -> {
            String normalizedKnownAlias = knownAlias.trim().toUpperCase();
            double distance = levenshtein.apply(normalizedSearchAlias, normalizedKnownAlias);
            if (distance == -1)
                return Map.entry(dict, 0.0);
            int maxLength = Math.max(normalizedSearchAlias.length(), normalizedKnownAlias.length());
            if (maxLength == 0)
                return Map.entry(dict, 1.0);
            double similarityScore = (maxLength - distance) / maxLength;
            return Map.entry(dict, similarityScore);
        })).filter(entry -> entry.getValue() >= minRequiredStringSimilarity).max(Map.Entry.comparingByValue()).map(Map.Entry::getKey);
    }

    public ProductDictEntity save(ProductDictEntity productDictEntity) {
        return productDictRepository.save(productDictEntity);
    }

    public List<ProductDictEntity> saveAll(Set<ProductDictEntity> productDictEntities) {
        return productDictRepository.saveAll(productDictEntities);
    }


}
