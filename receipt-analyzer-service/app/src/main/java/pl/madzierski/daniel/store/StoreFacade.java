package pl.madzierski.daniel.store;

import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.store.model.*;

import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
public class StoreFacade {

    private final StoreQueryRepository storeQueryRepository;
    private final StoreRepository storeRepository;
    private final StoreBrandRepository storeBrandRepository;
    private final StoreBrandQueryRepository storeBrandQueryRepository;

    StoreDetailsResponse getStoreDetails(UUID storeId) {
        return storeQueryRepository.findStoreById(storeId).stream().map(StoreDetailsResponse::storeMapper).findFirst().orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.STORE_NOT_FOUND));
    }

    StoreListResponse getStores(String query) {
        return new StoreListResponse(
            storeQueryRepository.findStoresByQuery(query).stream().map(
                StoreListResponse::storeMapper).collect(Collectors.toSet())
        );
    }

    @Transactional
    CreateStoreResponse addStore(CreateStoreRequest request) {
        StoreBrandQuery brand = request.storeBrandId() != null ? existingBrand(request.storeBrandId()) : newBrand(request.brandName());
        Store store = storeRepository.save(Store.create(brand, request.address(), request.city(), request.postalCode(),
            request.country()));
        return new CreateStoreResponse(store.getId());
    }

    private StoreBrandQuery existingBrand(UUID storeBrandId) {
        return storeBrandRepository.findById(storeBrandId)
            .map(brand -> new StoreBrandQuery(brand.getId()))
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.STORE_BRAND_NOT_FOUND));
    }

    private StoreBrandQuery newBrand(String brandName) {
        if (brandName == null || brandName.isBlank())
            throw new AppRuntimeException(AppRuntimeExceptionMessages.INVALID_REQUEST);
        return new StoreBrandQuery(storeBrandRepository.save(StoreBrand.create(brandName)).getId());
    }

    public StoreBrandListResponse getStoreBrands(String query) {
        return new StoreBrandListResponse(storeBrandQueryRepository.findStoreBrandsByQuery(query).stream().map(StoreBrandListResponse::storeBrandMapper).collect(Collectors.toSet()));
    }
}
