package pl.madzierski.daniel.app.product_dict;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.madzierski.daniel.app.product_dict.model.GetProductDictListResponse;
import pl.madzierski.daniel.app.product_dict.model.UpdateProductDictListRequest;
import pl.madzierski.daniel.app.product_dict.model.UpdateProductDictListResponse;
import pl.madzierski.daniel.app.product_dict.product_alias.ProductAliasProvider;
import pl.madzierski.daniel.app.product_dict.product_alias.ProductAliasEntity;
import pl.madzierski.daniel.app.product_dict.product_category.ProductCategoryEntity;
import pl.madzierski.daniel.app.product_dict.product_category.ProductCategoryProvider;
import pl.madzierski.daniel.app.receipt.revision.item.ReceiptItemProvider;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;

import java.util.*;

@Service
@AllArgsConstructor
class ProductDictService {

    private final ProductDictRepository productDictRepository;
    private final ProductAliasProvider productAliasProvider;
    private final ReceiptItemProvider receiptItemProvider;
    private final ProductCategoryProvider productCategoryProvider;

    public GetProductDictListResponse getProductDictList() {
        Set<ProductDictEntity> allCacheable = productDictRepository.findAllCacheable();
        return new GetProductDictListResponse(allCacheable.stream().map(productDict -> new GetProductDictListResponse.ProductDict(
                productDict.getId(),
                productDict.getName(),
                productDict.getProductCategory() == null ? null : productDict.getProductCategory().getId(),
                productDict.getAliases().stream().map(alias -> new GetProductDictListResponse.ProductDict.Alias(alias.getId(), alias.getName())).toList()
        )).toList());
    }

    @Transactional
    public UpdateProductDictListResponse updateProductDict(UpdateProductDictListRequest updateProductDictListRequest) {
        return new UpdateProductDictListResponse(updateProductDictListRequest.items().stream().map(updateProductDict -> {
            List<String> dictIds = updateProductDict.productDictList();
            String primaryDictId = dictIds.getFirst();

            ProductDictEntity primaryDict = productDictRepository.findById(primaryDictId).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_DICT_NOT_FOUND));
            primaryDict.setName(updateProductDict.canonicalName().trim());
            primaryDict.setProductCategory(resolveProductCategory(updateProductDict.productCategoryId()));

            if (dictIds.size() > 1) {
                List<String> productDictIdsListToMerge = dictIds.subList(1, dictIds.size());
                Set<String> existingAliasNames = new HashSet<>();
                primaryDict.getAliases().forEach(alias -> existingAliasNames.add(alias.getName()));
                productAliasProvider.getAliasesByProductDictIdList(productDictIdsListToMerge).forEach(alias -> addAliasIfMissing(primaryDict, existingAliasNames, alias));
                receiptItemProvider.reassignProductDict(primaryDict, productDictIdsListToMerge);
                productDictRepository.flush();
                productDictRepository.deleteAllByIdInBatch(productDictIdsListToMerge);
            }

            List<UpdateProductDictListResponse.UpdateProductDict.Alias> responseAliases = primaryDict.getAliases().stream().map(alias -> new UpdateProductDictListResponse.UpdateProductDict.Alias(alias.getId(), alias.getName())).toList();
            return new UpdateProductDictListResponse.UpdateProductDict(primaryDict.getId(), primaryDict.getName(), primaryDict.getProductCategory() == null ? null : primaryDict.getProductCategory().getId(), responseAliases);
        }).toList());
    }

    private ProductCategoryEntity resolveProductCategory(String productCategoryId) {
        if (productCategoryId == null || productCategoryId.isBlank()) {
            return null;
        }

        return productCategoryProvider.findProductCategoryById(productCategoryId)
                .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_CATEGORY_NOT_FOUND));
    }

    private void addAliasIfMissing(ProductDictEntity primaryDict, Set<String> existingAliasNames, ProductAliasEntity alias) {
        if (existingAliasNames.add(alias.getName())) {
            primaryDict.addAlias(alias);
        }
    }
}