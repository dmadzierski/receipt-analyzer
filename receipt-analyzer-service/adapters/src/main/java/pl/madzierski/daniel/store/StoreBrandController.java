package pl.madzierski.daniel.store;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.madzierski.daniel.store.model.StoreBrandListResponse;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/store-brands", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class StoreBrandController {

    private final StoreFacade storeFacade;

    @GetMapping
    ResponseEntity<StoreBrandListResponse> getStoreBrands(@RequestParam String query) {
        return ResponseEntity.ok(this.storeFacade.getStoreBrands(query));
    }

}
