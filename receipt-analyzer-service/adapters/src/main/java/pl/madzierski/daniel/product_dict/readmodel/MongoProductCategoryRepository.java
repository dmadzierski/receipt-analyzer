package pl.madzierski.daniel.product_dict.readmodel;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.annotation.Collation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.product_dict.ProductCategoryQueryRepository;
import pl.madzierski.daniel.product_dict.model.ProductCategoryDto;

import java.util.List;
import java.util.UUID;

interface MongoProductCategoryRepository extends MongoRepository<MongoProductCategory, UUID> {

    @Collation("pl")
    List<MongoProductCategory> findAllByOrderByNameAsc();
}

@Repository
@RequiredArgsConstructor
class ProductCategoryQueryRepositoryImpl implements ProductCategoryQueryRepository {

    private final MongoProductCategoryRepository repository;

    @Override
    public List<ProductCategoryDto> findAllOrderedByName() {
        return repository.findAllByOrderByNameAsc().stream().map(MongoProductCategory::toDto).toList();
    }
}
