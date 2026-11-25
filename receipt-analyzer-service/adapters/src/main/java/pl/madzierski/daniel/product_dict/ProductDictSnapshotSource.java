package pl.madzierski.daniel.product_dict;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.madzierski.daniel.event.IntegrationEvent;
import pl.madzierski.daniel.readmodel.ReadModelSnapshotSource;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
class ProductDictSnapshotSource implements ReadModelSnapshotSource {

    private final SqlProductCategoryRepository categoryRepository;
    private final SqlProductDictRepository productDictRepository;

    @Override
    public int order() {
        return 30;
    }

    @Override
    public Stream<IntegrationEvent> snapshots() {
        return Stream.concat(
            categoryRepository.findAll().stream().map(SqlProductCategory::toProductCategory).map(ProductDictEventMapper::changed),
            productDictRepository.findAllWithAliasesAndCategories().stream().map(SqlProductDict::toProductDict).map(ProductDictEventMapper::changed));
    }
}
