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
import pl.madzierski.daniel.user.model.UserQuery;

import java.util.*;

@AllArgsConstructor
public class ProductDictFacade {

    private final ProductDictRepository productDictRepository;
    private final ProductDictQueryRepository productDictQueryRepository;
    private final ProductDictFactory productDictFactory;
    private final ProductAliasRepository productAliasRepository;
    private final ProductAliasQueryRepository productAliasQueryRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ReceiptFacade receiptFacade;
    private final Double minRequiredStringSimilarity;
    private final UserQueryRepository userQueryRepository;

    GetProductDictListResponse getProductDictList() {
        Set<ProductDictWithAliasesAndCategoryProjection> allWithCategoryAndAliases = productDictQueryRepository.findAllWithCategoryAndAliases();
        return new GetProductDictListResponse(allWithCategoryAndAliases.stream().map(productDict -> new GetProductDictListResponse.ProductDict(
            productDict.getId(),
            productDict.getName(),
            productDict.getCategories().stream().map(category -> new GetProductDictListResponse.ProductDict.ProductCategory(
                category.getId(),
                category.getName()
            )).toList(),
            productDict.getAliases().stream().map(alias -> new GetProductDictListResponse.ProductDict.Alias(alias.getId(), alias.getName())).toList()
        )).toList());
    }

    @Transactional
    void updateProductDict(UpdateProductDictListRequest updateProductDictListRequest) {
        updateProductDictListRequest.items().forEach(updateProductDict -> {
            List<String> dictIds = updateProductDict.productDictList();
            String primaryDictId = dictIds.getFirst();
            ProductDict productDict = productDictRepository.findById(primaryDictId).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_NOT_FOUND));
            productDict.setName(updateProductDict.canonicalName().trim());
            if (updateProductDict.productCategoryId() != null) {
                ProductCategory productCategory =
                    productCategoryRepository.findById(updateProductDict.productCategoryId()).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_CATEGORY_NOT_FOUND));
                productDict.setProductCategories(Set.of(productCategory));
            }
            if (dictIds.size() > 1) {
                List<String> productDictIdsListToMerge = dictIds.subList(1, dictIds.size());
                this.mergeProductAliasesOfProductDictList(productDict.getId(), productDictIdsListToMerge);
                receiptFacade.reassignProductDict(toDto(productDict), productDictIdsListToMerge);
                productDictRepository.deleteAllByIdIn(productDictIdsListToMerge);
            }
        });
    }

    private ProductDictQuery toDto(ProductDict productDict) {
        return new ProductDictQuery(productDict.getId());
    }


    public Optional<ProductDto> findCanonicalName(String alias) {
        Set<ProductAliasDto> allAliases = productAliasQueryRepository.findAllAliases();
        Optional<ProductAliasDto> productDictOptional = allAliases.stream().filter(aliasDto -> aliasDto.getName().equalsIgnoreCase(alias)).findFirst();
        if (productDictOptional.isPresent())
            return Optional.of(new ProductDto(productDictOptional.get().getId()));

        String normalizedSearchAlias = alias.trim().toUpperCase();
        int searchLength = normalizedSearchAlias.length();
        return allAliases.parallelStream()
            .map(aliasDto -> {
                    String normalizedKnownAlias = aliasDto.getName().trim().toUpperCase();
                    int knownLength = normalizedKnownAlias.length();
                    int maxLength = Math.max(searchLength, knownLength);
                    if (maxLength == 0)
                        return Map.entry(aliasDto, 1.0);
                    int maxAllowedDifference = (int) Math.ceil(maxLength * (1.0 - minRequiredStringSimilarity));
                    double distance = new LevenshteinDistance(maxAllowedDifference).apply(normalizedSearchAlias, normalizedKnownAlias);
                    if (distance == -1) {
                        return Map.entry(aliasDto, 0.0);
                    }
                    double similarityScore = (maxLength - distance) / maxLength;
                    return Map.entry(aliasDto, similarityScore);
                }
            )
            .filter(entry -> entry.getValue() >= minRequiredStringSimilarity)
            .max(Map.Entry.comparingByValue())
            .map(productAliasDtoDoubleEntry -> new ProductDto(productAliasDtoDoubleEntry.getKey().getProductDictId()));
    }

    public void mergeProductAliasesOfProductDictList(String productDictId, List<String> productDictIdsListToMerge) {
        productAliasRepository.reassignAliasesToProductDict(productDictId, productDictIdsListToMerge);
    }

    public void saveAll(Collection<ProductDto> productDictEntities) {
        productDictRepository.saveAll(productDictEntities.stream().map(productDictFactory::from).toList());
    }

    public long countByProductCategoryId(String id) {
        return productDictQueryRepository.countProductDictEntitiesByCategoriesIdIn(Set.of(id));
    }

    GetProductCategoryListResponse getProductCategoryList() {
        List<GetProductCategoryListResponse.ProductCategory> items = productCategoryRepository.findAllByOrderByNameAsc().stream()
            .map(productCategory -> new GetProductCategoryListResponse.ProductCategory(productCategory.getId(), productCategory.getName()))
            .toList();
        return new GetProductCategoryListResponse(items);
    }

    @Transactional
    CreateProductCategoryResponse addProductCategory(String userSub, CreateProductCategoryRequest request) {
        String name = request.name().trim();
        if (productCategoryRepository.existsByNameAndUser(name, userSub))
            throw new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_CATEGORY_ALREADY_EXISTS);
        UserQuery user = userQueryRepository.findUserByUserSub(userSub).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.USER_NOT_FOUND));
        ProductCategory savedProductCategory = productCategoryRepository.save(new ProductCategory(name, user));
        return new CreateProductCategoryResponse(savedProductCategory.getId(), savedProductCategory.getName());
    }

    @Transactional
    UpdateProductCategoryResponse updateProductCategory(String productCategoryId, UpdateProductCategoryRequest request) {
        ProductCategory productCategory = productCategoryRepository.findById(productCategoryId)
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_CATEGORY_NOT_FOUND));
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
        ProductCategory productCategory = productCategoryRepository.findById(productCategoryId)
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_CATEGORY_NOT_FOUND));
        if (this.countByProductCategoryId(productCategory.getId()) > 0) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_CATEGORY_IN_USE);
        }
        productCategoryRepository.delete(productCategory);
    }

}
