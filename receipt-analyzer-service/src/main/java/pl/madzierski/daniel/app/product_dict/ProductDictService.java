package pl.madzierski.daniel.app.product_dict;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.madzierski.daniel.app.product_dict.model.GetProductDictListResponse;
import pl.madzierski.daniel.app.product_dict.model.UpdateProductDictRequest;
import pl.madzierski.daniel.app.product_dict.model.UpdateProductDictResponse;
import pl.madzierski.daniel.app.product_dict.product_alias.ProductAliasEntity;
import pl.madzierski.daniel.app.product_dict.product_alias.ProductAliasProvider;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;

import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
class ProductDictService {

    private final ProductDictRepository productDictRepository;
    private final ProductAliasProvider productAliasProvider;

    public GetProductDictListResponse getProductDictList() {
        Set<ProductDictEntity> allCacheable = productDictRepository.findAllCacheable();
        return new GetProductDictListResponse(allCacheable.stream().map(productDict -> new GetProductDictListResponse.ProductDict(
                productDict.getId(),
                productDict.getName(),
                productDict.getAliases().stream().map(alias -> new GetProductDictListResponse.ProductDict.Alias(alias.getId(), alias.getName())).toList()
        )).toList());
    }

    @Transactional
    public UpdateProductDictResponse updateProductDict(UpdateProductDictRequest updateProductDictRequest) {
        List<String> dictIds = updateProductDictRequest.productDictList();
        String primaryDictId = dictIds.getFirst();

        ProductDictEntity primaryDict = productDictRepository.findById(primaryDictId).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_DICT_NOT_FOUND));
        primaryDict.setName(updateProductDictRequest.canonicalName());

        if (dictIds.size() > 1) {
            List<String> productDictIdsListToMerge = dictIds.subList(1, dictIds.size());
            productAliasProvider.getAliasesByProductDictIdList(productDictIdsListToMerge).forEach(primaryDict::addAlias);
            productDictRepository.flush();
            productDictRepository.deleteAllByIdInBatch(productDictIdsListToMerge);
        }

        List<UpdateProductDictResponse.Alias> responseAliases = primaryDict.getAliases().stream().map(alias -> new UpdateProductDictResponse.Alias(alias.getId(), alias.getName())).toList();
        return new UpdateProductDictResponse(primaryDict.getId(), primaryDict.getName(), responseAliases);
    }
}