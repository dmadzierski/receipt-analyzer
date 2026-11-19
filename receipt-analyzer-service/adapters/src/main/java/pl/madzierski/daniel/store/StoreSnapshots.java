package pl.madzierski.daniel.store;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.madzierski.daniel.event.IntegrationEvent;
import pl.madzierski.daniel.readmodel.ReadModelSnapshotSource;
import pl.madzierski.daniel.store.event.StoreBrandChanged;
import pl.madzierski.daniel.store.event.StoreChanged;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
class StoreSnapshots implements ReadModelSnapshotSource {

    private final SqlStoreBrandRepository brandRepository;
    private final SqlStoreRepository storeRepository;

    static StoreBrandChanged changed(StoreBrand brand) {
        return new StoreBrandChanged(brand.getId(), brand.getVersion(), brand.getName());
    }

    static StoreChanged changed(Store store) {
        return new StoreChanged(store.getId(), store.getVersion(), store.getBrand() != null ? store.getBrand().id() : null, store.getAddress(),
            store.getCity(), store.getPostalCode(), store.getCountry());
    }

    @Override
    public int order() {
        return 20;
    }

    @Override
    public Stream<IntegrationEvent> snapshots() {
        return Stream.concat(
            brandRepository.findAll().stream().map(SqlStoreBrand::toStoreBrand).map(StoreSnapshots::changed),
            storeRepository.findAll().stream().map(SqlStore::toStore).map(StoreSnapshots::changed));
    }
}
