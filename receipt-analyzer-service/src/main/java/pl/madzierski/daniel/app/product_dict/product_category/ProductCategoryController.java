package pl.madzierski.daniel.app.product_dict.product_category;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.madzierski.daniel.app.product_dict.product_category.model.*;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/product-categories", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    @GetMapping
    ResponseEntity<GetProductCategoryListResponse> getProductCategoryList() {
        return ResponseEntity.ok(productCategoryService.getProductCategoryList());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CreateProductCategoryResponse> addProductCategory(@RequestBody @Valid CreateProductCategoryRequest request) {
        return ResponseEntity.ok(productCategoryService.addProductCategory(request));
    }

    @PutMapping(path = "/{productCategoryId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UpdateProductCategoryResponse> updateProductCategory(@PathVariable String productCategoryId, @RequestBody @Valid UpdateProductCategoryRequest request) {
        return ResponseEntity.ok(productCategoryService.updateProductCategory(productCategoryId, request));
    }

    @DeleteMapping(path = "/{productCategoryId}")
    ResponseEntity<Void> deleteProductCategory(@PathVariable String productCategoryId) {
        productCategoryService.deleteProductCategory(productCategoryId);
        return ResponseEntity.noContent().build();
    }
}