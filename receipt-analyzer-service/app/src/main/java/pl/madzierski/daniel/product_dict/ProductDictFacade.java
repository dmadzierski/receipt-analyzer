package pl.madzierski.daniel.product_dict;

import lombok.AllArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.transaction.annotation.Transactional;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.product_dict.model.*;
import pl.madzierski.daniel.product_dict.projection.ProductDictWithAliasesAndCategoryProjection;
import pl.madzierski.daniel.receipt.ReceiptFacade;
import pl.madzierski.daniel.user.UserQueryRepository;
import pl.madzierski.daniel.user.model.UserDto;
import pl.madzierski.daniel.user.model.UserQuery;

import java.util.*;

@AllArgsConstructor
public class ProductDictFacade {

    private final ProductDictRepository productDictRepository;
    private final ProductDictQueryRepository productDictQueryRepository;
    private final ProductDictFactory productDictFactory;
    private final ProductAliasRepository productAliasRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ReceiptFacade receiptFacade;
    private final Double minRequiredStringSimilarity;
    private final UserQueryRepository userQueryRepository;

    GetProductDictListResponse getProductDictList() {
        Set<ProductDictWithAliasesAndCategoryProjection> allWithCategoryAndAliases = productDictQueryRepository.findAllWithCategoryAndAliases();
        return new GetProductDictListResponse(allWithCategoryAndAliases.stream().map(productDict -> new GetProductDictListResponse.ProductDict(productDict.getId(), productDict.getName(), productDict.getCategories().stream().map(category -> new GetProductDictListResponse.ProductDict.ProductCategory(category.getId(), category.getName())).toList(), productDict.getAliases().stream().map(alias -> new GetProductDictListResponse.ProductDict.Alias(alias.getId(), alias.getName())).toList())).toList());
    }

    @Transactional
    void updateProductDict(UpdateProductDictListRequest body) {
        body.items().forEach(item -> {
            List<String> dictIds = item.productDictList();
            String primaryId = dictIds.getFirst();
            List<String> sourceIds = dictIds.subList(1, dictIds.size());

            ProductDict product = productDictRepository.findById(primaryId)
                .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_NOT_FOUND));

            String canonicalName = item.canonicalName().trim();
            updateAliases(product, canonicalName, item.aliasIds());
            product.setProductCategories(loadCategories(item.productCategoryIds()));
            productDictRepository.save(product);

            mergeProducts(primaryId, sourceIds);
        });
    }

    private void updateAliases(ProductDict product, String name, Set<String> aliasIds) {
        Set<ProductAlias> previous = new HashSet<>(product.getAliases());
        Set<ProductAlias> chosen = new HashSet<>(productAliasRepository.findAllByIds(aliasIds));

        boolean hasCanonical = chosen.stream().anyMatch(a -> a.getName().equalsIgnoreCase(name));
        if (!hasCanonical) {
            chosen.add(previous.stream()
                .filter(a -> a.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> ProductAlias.builder().name(name).build()));
        }

        product.setAliases(chosen);
        product.setName(name);
    }

    private void mergeProducts(String primaryId, List<String> sourceIds) {
        if (sourceIds.isEmpty()) return;
        receiptFacade.reassignProductDict(new ProductDictQuery(primaryId), sourceIds);
        productDictRepository.deleteAllByIdIn(sourceIds);
    }

    private Set<ProductCategory> loadCategories(Collection<String> ids) {
        Set<String> requested = ids == null ? Set.of() : new HashSet<>(ids);
        Set<ProductCategory> found = productCategoryRepository.findAllById(requested);
        if (found.size() != requested.size()) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_CATEGORY_NOT_FOUND);
        }
        return found;
    }

    public Optional<ProductDto> findCanonicalName(String alias, String userTranslation) {
        List<ProductDto> productDtoList = productDictQueryRepository.findAllProducts();
        Optional<ProductDto> exactMatch = productDtoList.stream().filter(productDto -> productDto.getAliases().stream().anyMatch(productAliasDto -> productAliasDto.getName().equalsIgnoreCase(alias) || productAliasDto.getName().equalsIgnoreCase(userTranslation))).findAny();
        if (exactMatch.isPresent()) return exactMatch;
        String normalizedSearchAlias = alias.trim().toUpperCase();
        int searchLength = normalizedSearchAlias.length();
        return productDtoList.parallelStream().flatMap(productDto -> productDto.getAliases().stream().map(aliasDto -> {
            String normalizedKnownAlias = aliasDto.getName().trim().toUpperCase();
            int knownLength = normalizedKnownAlias.length();
            int maxLength = Math.max(searchLength, knownLength);
            if (maxLength == 0) return Map.entry(productDto, 1.0);
            int maxAllowedDifference = (int) Math.ceil(maxLength * (1.0 - minRequiredStringSimilarity));
            double distance = new LevenshteinDistance(maxAllowedDifference).apply(normalizedSearchAlias, normalizedKnownAlias);
            if (distance == -1) {
                return Map.entry(productDto, 0.0);
            }
            double similarityScore = (maxLength - distance) / maxLength;
            return Map.entry(productDto, similarityScore);
        })).filter(entry -> entry.getValue() >= minRequiredStringSimilarity).max(Map.Entry.comparingByValue()).map(Map.Entry::getKey);
    }

    public List<ProductDto> saveAll(Set<ProductDto> productDictEntities) {
        return productDictRepository.saveAll(productDictEntities.stream().map(productDictFactory::from).toList())
                .stream().map(product -> ProductDto.builder().id(product.getId()).name(product.getName()).build()).toList();
    }

    GetProductCategoryListResponse getProductCategoryList() {
        List<GetProductCategoryListResponse.ProductCategory> items = productCategoryRepository.findAllByOrderByNameAsc().stream().map(productCategory -> new GetProductCategoryListResponse.ProductCategory(productCategory.getId(), productCategory.getName())).toList();
        return new GetProductCategoryListResponse(items);
    }

    @Transactional
    CreateProductCategoryResponse addProductCategory(String userSub, CreateProductCategoryRequest request) {
        String name = request.name().trim();
        if (productCategoryRepository.existsByNameAndUser(name, userSub))
            throw new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_CATEGORY_ALREADY_EXISTS);
        UserDto user = userQueryRepository.findUser(userSub).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.USER_NOT_FOUND));
        ProductCategory savedProductCategory = productCategoryRepository.save(new ProductCategory(name, new UserQuery(user.id())));
        return new CreateProductCategoryResponse(savedProductCategory.getId(), savedProductCategory.getName());
    }

    @Transactional
    UpdateProductCategoryResponse updateProductCategory(String productCategoryId, UpdateProductCategoryRequest request) {
        ProductCategory productCategory = productCategoryRepository.findById(productCategoryId).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_CATEGORY_NOT_FOUND));
        String name = request.name().trim();

        if (!productCategory.getName().equals(name) && productCategoryRepository.existsByNameAndIdNot(name, productCategoryId)) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_CATEGORY_ALREADY_EXISTS);
        }

        productCategory.setName(name);
        ProductCategory savedProductCategory = productCategoryRepository.save(productCategory);
        return new UpdateProductCategoryResponse(savedProductCategory.getId(), savedProductCategory.getName());
    }

    @Transactional
    void deleteProductCategory(String productCategoryId) {
        ProductCategory productCategory = productCategoryRepository.findById(productCategoryId).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_CATEGORY_NOT_FOUND));
        if (productDictQueryRepository.countProductDictEntitiesByCategoriesIdIn(Set.of(productCategory.getId())) > 0) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_CATEGORY_IN_USE);
        }
        productCategoryRepository.delete(productCategory);
    }

}
