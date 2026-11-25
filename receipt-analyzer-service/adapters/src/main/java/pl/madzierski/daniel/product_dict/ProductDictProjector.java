package pl.madzierski.daniel.product_dict;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.madzierski.daniel.event.IntegrationEvent;
import pl.madzierski.daniel.product_dict.event.ProductCategoryChanged;
import pl.madzierski.daniel.product_dict.event.ProductCategoryDeleted;
import pl.madzierski.daniel.product_dict.event.ProductDictChanged;
import pl.madzierski.daniel.product_dict.event.ProductDictDeleted;
import pl.madzierski.daniel.readmodel.Projector;

import java.util.UUID;

@Component
@RequiredArgsConstructor
class ProductDictProjector implements Projector {

    private final MongoProductCategoryRepository categoryRepository;
    private final MongoProductDictRepository productDictRepository;

    @Override
    public void apply(IntegrationEvent event) {
        switch (event) {
            case ProductCategoryChanged changed -> {
                categoryRepository.save(MongoProductCategory.fromEvent(changed));
                productDictRepository.renameCategory(changed.id(), changed.name());
            }
            case ProductCategoryDeleted deleted -> categoryRepository.deleteById(deleted.id());
            case ProductDictChanged changed -> productDictRepository.save(MongoProductDict.fromEvent(changed, this::categoryName));
            case ProductDictDeleted deleted -> productDictRepository.deleteById(deleted.id());
            default -> {
            }
        }
    }

    @Override
    public void clear() {
        productDictRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    private String categoryName(UUID categoryId) {
        return categoryRepository.findById(categoryId).map(MongoProductCategory::name).orElse(null);
    }
}
