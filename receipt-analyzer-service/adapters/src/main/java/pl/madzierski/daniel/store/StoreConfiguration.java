package pl.madzierski.daniel.store;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.madzierski.daniel.store.model.StoreRepository;

@Configuration
public class StoreConfiguration {

    @Bean
    StoreFacade storeFacade(StoreQueryRepository storeQueryRepository, StoreRepository storeRepository, StoreBrandRepository storeBrandRepository, StoreBrandQueryRepository storeBrandQueryRepository) {
        return new StoreFacade(storeQueryRepository, storeRepository, storeBrandRepository, storeBrandQueryRepository);
    }
}
