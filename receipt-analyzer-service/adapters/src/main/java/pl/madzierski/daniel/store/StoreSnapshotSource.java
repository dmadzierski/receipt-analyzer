package pl.madzierski.daniel.store;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.madzierski.daniel.event.IntegrationEvent;
import pl.madzierski.daniel.readmodel.ReadModelSnapshotSource;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
class StoreSnapshotSource implements ReadModelSnapshotSource {

    private final SqlStoreBrandRepository brandRepository;
    private final SqlStoreRepository storeRepository;

    @Override
    public int order() {
        return 20;
    }

    @Override
    public Stream<IntegrationEvent> snapshots() {
        return Stream.concat(
            brandRepository.findAll().stream().map(SqlStoreBrand::toStoreBrand).map(StoreEventMapper::changed),
            storeRepository.findAll().stream().map(SqlStore::toStore).map(StoreEventMapper::changed));
    }
}
