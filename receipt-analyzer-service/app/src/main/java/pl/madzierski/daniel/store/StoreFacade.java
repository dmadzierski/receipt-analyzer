package pl.madzierski.daniel.store;

import lombok.AllArgsConstructor;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.store.model.*;

import java.util.stream.Collectors;

@AllArgsConstructor
public class StoreFacade {

    private final StoreQueryRepository storeQueryRepository;
    private final StoreRepository storeRepository;
    private final StoreBrandRepository storeBrandRepository;
    private final StoreBrandQueryRepository storeBrandQueryRepository;

    StoreDetailsResponse getStoreDetails(String storeId) {
        return storeQueryRepository.findStoreById(storeId).stream().map(StoreDetailsResponse::storeMapper).findFirst().orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.STORE_NOT_FOUND));
    }

    public StoreListResponse getStores(String query) {
        return new StoreListResponse(
            storeQueryRepository.findStoresByQuery(query).stream().map(
                StoreListResponse::storeMapper).collect(Collectors.toSet())
        );
    }

    public CreateStoreResponse addStore(CreateStoreRequest request) {
        StoreBrand storeBrand;
        if (request.storeBrandId() != null)
            storeBrand = storeBrandRepository.findById(request.storeBrandId()).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.STORE_BRAND_NOT_FOUND));
       else
            storeBrand = storeBrandRepository.save(StoreBrand.builder().name(request.brandName()).build());
        Store store = Store.builder()
            .brand(storeBrand)
            .address(request.address())
            .city(request.city())
            .postalCode(request.postalCode())
            .country(request.country())
            .build();
        storeRepository.save(store);
        return new CreateStoreResponse(
            store.getId(),
            store.getBrand().getName(),
            store.getAddress(),
            store.getCity(),
            store.getPostalCode(),
            store.getCountry()
        );
    }

    public StoreBrandListResponse getStoreBrands(String query) {
        return new StoreBrandListResponse(storeBrandQueryRepository.findStoreBrandsByQuery(query).stream().map(StoreBrandListResponse::storeBrandMapper).collect(Collectors.toSet()));
    }
}
