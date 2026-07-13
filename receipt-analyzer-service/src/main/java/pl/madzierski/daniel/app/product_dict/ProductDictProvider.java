package pl.madzierski.daniel.app.product_dict;

import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductDictProvider {

    private final ProductDictRepository productDictRepository;
    @Value("${product-dict.min-required-similarity}")
    private Double minRequiredStringSimilarity;

    public Optional<ProductDictEntity> findCanonicalName(String alias) {
        Set<ProductDictEntity> allDictionaries = productDictRepository.findAllCacheable();
        Optional<ProductDictEntity> productDictOptional = allDictionaries.stream().filter(productDict -> productDict.getAliases().stream().anyMatch(knownAlias -> knownAlias.getName().equalsIgnoreCase(alias))).findFirst();
        if (productDictOptional.isPresent())
            return productDictOptional;

        String normalizedSearchAlias = alias.trim().toUpperCase();
        int searchLength = normalizedSearchAlias.length();
        return allDictionaries.parallelStream()
                .flatMap(dict -> dict.getAliases().stream()
                        .map(knownAlias -> {
                            String normalizedKnownAlias = knownAlias.getName().trim().toUpperCase();
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

    public List<ProductDictEntity> saveAll(Collection<ProductDictEntity> productDictEntities) {
        return productDictRepository.saveAll(productDictEntities);
    }

    public long countByProductCategory_Id(String id) {
        return productDictRepository.countProductDictEntitiesByProductCategoryId(id);
    }
}
