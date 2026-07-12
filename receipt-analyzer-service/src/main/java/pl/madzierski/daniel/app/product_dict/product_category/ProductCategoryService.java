package pl.madzierski.daniel.app.product_dict.product_category;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.madzierski.daniel.app.product_dict.ProductDictRepository;
import pl.madzierski.daniel.app.product_dict.product_category.model.*;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;

import java.util.List;

@Service
@AllArgsConstructor
class ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;
    private final ProductDictRepository productDictRepository;

    public GetProductCategoryListResponse getProductCategoryList() {
        List<GetProductCategoryListResponse.ProductCategory> items = productCategoryRepository.findAllByOrderByNameAsc().stream()
                .map(productCategory -> new GetProductCategoryListResponse.ProductCategory(productCategory.getId(), productCategory.getName()))
                .toList();
        return new GetProductCategoryListResponse(items);
    }

    @Transactional
    public CreateProductCategoryResponse addProductCategory(CreateProductCategoryRequest request) {
        String name = request.name().trim();
        if (productCategoryRepository.existsByName(name)) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_CATEGORY_ALREADY_EXISTS);
        }

        ProductCategoryEntity savedProductCategory = productCategoryRepository.save(new ProductCategoryEntity(name));
        return new CreateProductCategoryResponse(savedProductCategory.getId(), savedProductCategory.getName());
    }

    @Transactional
    public UpdateProductCategoryResponse updateProductCategory(String productCategoryId, UpdateProductCategoryRequest request) {
        ProductCategoryEntity productCategory = productCategoryRepository.findById(productCategoryId)
                .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_CATEGORY_NOT_FOUND));
        String name = request.name().trim();

        if (!productCategory.getName().equals(name) && productCategoryRepository.existsByNameAndIdNot(name, productCategoryId)) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_CATEGORY_ALREADY_EXISTS);
        }

        productCategory.setName(name);
        ProductCategoryEntity savedProductCategory = productCategoryRepository.save(productCategory);
        return new UpdateProductCategoryResponse(savedProductCategory.getId(), savedProductCategory.getName());
    }

    @Transactional
    public void deleteProductCategory(String productCategoryId) {
        ProductCategoryEntity productCategory = productCategoryRepository.findById(productCategoryId)
                .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_CATEGORY_NOT_FOUND));
        if (productDictRepository.countByProductCategory_Id(productCategory.getId()) > 0) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_CATEGORY_IN_USE);
        }
        productCategoryRepository.delete(productCategory);
    }

}