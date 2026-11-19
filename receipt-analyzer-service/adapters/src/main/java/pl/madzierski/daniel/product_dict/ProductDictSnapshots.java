package pl.madzierski.daniel.product_dict;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.madzierski.daniel.event.IntegrationEvent;
import pl.madzierski.daniel.product_dict.event.ProductCategoryChanged;
import pl.madzierski.daniel.product_dict.event.ProductDictChanged;
import pl.madzierski.daniel.readmodel.ReadModelSnapshotSource;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
class ProductDictSnapshots implements ReadModelSnapshotSource {

    private final SqlProductCategoryRepository categoryRepository;
    private final SqlProductDictRepository productDictRepository;

    static ProductCategoryChanged changed(ProductCategory category) {
        return new ProductCategoryChanged(category.getId(), category.getVersion(), category.getName());
    }

    static ProductDictChanged changed(ProductDict product) {
        return new ProductDictChanged(product.getId(), product.getVersion(), product.getName(),
            product.getAliases().stream().map(alias -> new ProductDictChanged.Alias(alias.getId(), alias.getName())).toList(),
            product.getProductCategories().stream().map(ProductCategory::getId).toList());
    }

    @Override
    public int order() {
        return 30;
    }

    @Override
    public Stream<IntegrationEvent> snapshots() {
        return Stream.concat(
            categoryRepository.findAll().stream().map(SqlProductCategory::toProductCategory).map(ProductDictSnapshots::changed),
            productDictRepository.findAllWithAliasesAndCategories().stream().map(SqlProductDict::toProductDict).map(ProductDictSnapshots::changed));
    }
}
