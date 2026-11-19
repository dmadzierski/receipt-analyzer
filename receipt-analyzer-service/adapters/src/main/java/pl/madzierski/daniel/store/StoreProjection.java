package pl.madzierski.daniel.store;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.madzierski.daniel.event.IntegrationEvent;
import pl.madzierski.daniel.readmodel.Projection;
import pl.madzierski.daniel.store.event.StoreBrandChanged;
import pl.madzierski.daniel.store.event.StoreChanged;

import java.util.UUID;

@Component
@RequiredArgsConstructor
class StoreProjection implements Projection {

    private final MongoStoreBrandRepository brandRepository;
    private final MongoStoreRepository storeRepository;

    @Override
    public void apply(IntegrationEvent event) {
        switch (event) {
            case StoreBrandChanged changed -> {
                brandRepository.save(MongoStoreBrand.fromEvent(changed));
                storeRepository.updateBrandName(changed.id(), changed.name());
            }
            case StoreChanged changed -> storeRepository.save(MongoStore.fromEvent(changed, brandName(changed.brandId())));
            default -> {
            }
        }
    }

    @Override
    public void clear() {
        storeRepository.deleteAll();
        brandRepository.deleteAll();
    }

    private String brandName(UUID brandId) {
        if (brandId == null) return null;
        return brandRepository.findById(brandId).map(MongoStoreBrand::name).orElse(null);
    }
}
