package pl.madzierski.daniel.store;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.madzierski.daniel.IntegrationTest;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.store.model.CreateStoreRequest;
import pl.madzierski.daniel.store.model.CreateStoreResponse;
import pl.madzierski.daniel.store.model.StoreBrandListResponse;
import pl.madzierski.daniel.store.model.StoreDetailsResponse;
import pl.madzierski.daniel.store.model.StoreDto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StoreFacadeIT extends IntegrationTest {

    @Autowired
    private StoreFacade storeFacade;

    @Autowired
    private StoreQueryRepository storeQueryRepository;

    @Test
    void createsStoreWithNewBrandAndReusesBrandForNextStore() {
        CreateStoreResponse first = storeFacade.addStore(
            new CreateStoreRequest(null, "Biedronka", "ul. Testowa 1", "Poznań", "60-001", "Polska"));
        flush();
        UUID brandId = jdbcTemplate.queryForObject("SELECT brand_id FROM store WHERE id = ?", UUID.class, first.id());

        CreateStoreResponse second = storeFacade.addStore(
            new CreateStoreRequest(brandId, null, "ul. Inna 2", "Gniezno", "62-200", "Polska"));
        flush();

        assertThat(second.brand()).isEqualTo("Biedronka");
        assertThat(count("SELECT count(*) FROM store_brand")).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT brand_id FROM store WHERE id = ?", UUID.class, second.id())).isEqualTo(brandId);
    }

    @Test
    void creatingStoreWithUnknownBrandFails() {
        CreateStoreRequest request = new CreateStoreRequest(UNKNOWN_ID, null, "ul. Testowa 1", "Poznań", "60-001", "Polska");

        assertError(() -> storeFacade.addStore(request), AppRuntimeExceptionMessages.STORE_BRAND_NOT_FOUND);
    }

    @Test
    void returnsStoreDetails() {
        UUID storeId = createStore("Lidl", "ul. Testowa 3", "Kraków");

        StoreDetailsResponse details = storeFacade.getStoreDetails(storeId);

        assertThat(details.brand()).isEqualTo("Lidl");
        assertThat(details.address()).isEqualTo("ul. Testowa 3");
        assertThat(details.city()).isEqualTo("Kraków");
    }

    @Test
    void unknownStoreFails() {
        assertError(() -> storeFacade.getStoreDetails(UNKNOWN_ID), AppRuntimeExceptionMessages.STORE_NOT_FOUND);
    }

    @Test
    void searchesBrandsRegardlessOfLetterCase() {
        createStore("Biedronka", "ul. Testowa 1", "Poznań");
        createStore("Lidl", "ul. Testowa 2", "Poznań");

        assertThat(storeFacade.getStoreBrands("BIEDR").storeBrands())
            .extracting(StoreBrandListResponse.StoreBrandItem::name)
            .containsExactly("Biedronka");
    }

    @Test
    void searchesStoresByBrandCityAndAddressRegardlessOfLetterCase() {
        UUID storeId = createStore("Biedronka", "ul. Półwiejska 2", "Poznań");

        assertThat(idsOf(storeQueryRepository.findStoresByQuery("biedronka"))).contains(storeId);
        assertThat(idsOf(storeQueryRepository.findStoresByQuery("POZNAŃ"))).contains(storeId);
        assertThat(idsOf(storeQueryRepository.findStoresByQuery("półwiejska"))).contains(storeId);
    }

    @Test
    void findsOnlyRequestedStoresByIds() {
        UUID first = createStore("Biedronka", "ul. Testowa 1", "Poznań");
        UUID second = createStore("Lidl", "ul. Testowa 2", "Gniezno");
        createStore("Żabka", "ul. Testowa 3", "Września");

        assertThat(idsOf(storeQueryRepository.findStoresByIds(List.of(first, second)))).containsExactlyInAnyOrder(first, second);
    }

    private static List<UUID> idsOf(Set<StoreDto> stores) {
        return stores.stream().map(StoreDto::getId).toList();
    }
}
