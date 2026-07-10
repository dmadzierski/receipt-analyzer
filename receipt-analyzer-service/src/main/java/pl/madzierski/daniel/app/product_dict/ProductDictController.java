package pl.madzierski.daniel.app.product_dict;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.madzierski.daniel.app.product_dict.model.GetProductDictListResponse;
import pl.madzierski.daniel.app.product_dict.model.UpdateProductDictRequest;
import pl.madzierski.daniel.app.product_dict.model.UpdateProductDictResponse;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/product-dicts", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
class ProductDictController {

    private final ProductDictService productDictService;

    @GetMapping
    ResponseEntity<GetProductDictListResponse> getProductDictList() {
        return ResponseEntity.ok(productDictService.getProductDictList());
    }

    @PostMapping
    ResponseEntity<UpdateProductDictResponse> updateProductDict(@RequestBody UpdateProductDictRequest updateProductDictRequest){
        return ResponseEntity.ok(productDictService.updateProductDict(updateProductDictRequest));
    }
}