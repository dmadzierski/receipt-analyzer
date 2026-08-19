package pl.madzierski.daniel.store;


import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.madzierski.daniel.store.model.CreateStoreRequest;
import pl.madzierski.daniel.store.model.CreateStoreResponse;
import pl.madzierski.daniel.store.model.StoreDetailsResponse;
import pl.madzierski.daniel.store.model.StoreListResponse;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/stores", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
class StoreController {

    private final StoreFacade storeFacade;

    @GetMapping(path = {"/{storeId}"})
    ResponseEntity<StoreDetailsResponse> getStoreDetails(@PathVariable String storeId) {
        return ResponseEntity.ok(this.storeFacade.getStoreDetails(storeId));
    }

    @GetMapping
    ResponseEntity<StoreListResponse> getStores(@RequestParam String query) {
        return ResponseEntity.ok(this.storeFacade.getStores(query));
    }

    @PostMapping
    ResponseEntity<CreateStoreResponse> addStore(@RequestBody CreateStoreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.storeFacade.addStore(request));
    }

}
