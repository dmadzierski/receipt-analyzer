package pl.madzierski.daniel.store;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.madzierski.daniel.IntegrationTest;
import pl.madzierski.daniel.store.event.StoreBrandChanged;
import pl.madzierski.daniel.store.event.StoreChanged;
import pl.madzierski.daniel.store.model.StoreDto;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class StoreProjectorIT extends IntegrationTest {

    @Autowired
    private StoreQueryRepository storeQueryRepository;

    @Test
    void renamedBrandIsShownInAllItsStores() {
        UUID brandId = UUID.randomUUID();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        UUID otherBrandStore = UUID.randomUUID();
        readModelProjector.project(new StoreBrandChanged(brandId, 1, "Biedronka"));
        readModelProjector.project(new StoreChanged(first, 1, brandId, "ul. Testowa 1", "Poznań", "60-001", "Polska"));
        readModelProjector.project(new StoreChanged(second, 1, brandId, "ul. Testowa 2", "Gniezno", "62-200", "Polska"));
        readModelProjector.project(new StoreChanged(otherBrandStore, 1, null, "ul. Testowa 3", "Września", "62-300", "Polska"));

        readModelProjector.project(new StoreBrandChanged(brandId, 2, "Biedronka Express"));

        assertThat(storeQueryRepository.findStoresByIds(List.of(first, second, otherBrandStore)))
            .extracting(StoreDto::getId, StoreDto::getBrand)
            .containsExactlyInAnyOrder(
                tuple(first, "Biedronka Express"),
                tuple(second, "Biedronka Express"),
                tuple(otherBrandStore, null));
    }
}
