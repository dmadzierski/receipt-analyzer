package pl.madzierski.daniel.product_dict;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.madzierski.daniel.product_dict.model.GetProductDictListResponse;
import pl.madzierski.daniel.product_dict.model.UpdateProductDictListRequest;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/product-dicts", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
class ProductDictController {

    private final ProductDictFacade productDictFacade;

    @GetMapping
    ResponseEntity<GetProductDictListResponse> getProductDictList() {
        return ResponseEntity.ok(productDictFacade.getProductDictList());
    }

    @PostMapping
    ResponseEntity<Void> updateProductDict(@RequestBody UpdateProductDictListRequest updateProductDictListRequest) {
        productDictFacade.updateProductDict(updateProductDictListRequest);
        return ResponseEntity.ok().build();
    }
}