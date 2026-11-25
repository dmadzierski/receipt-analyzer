package pl.madzierski.daniel.product_dict;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.madzierski.daniel.IntegrationTest;
import pl.madzierski.daniel.product_dict.event.ProductCategoryChanged;
import pl.madzierski.daniel.product_dict.event.ProductDictChanged;
import pl.madzierski.daniel.product_dict.model.ProductDictDto;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ProductDictProjectorIT extends IntegrationTest {

    @Autowired
    private ProductDictQueryRepository productDictQueryRepository;

    @Test
    void renamedCategoryIsShownInAllItsProducts() {
        UUID dairy = UUID.randomUUID();
        UUID organic = UUID.randomUUID();
        UUID milk = UUID.randomUUID();
        UUID cheese = UUID.randomUUID();
        readModelProjector.project(new ProductCategoryChanged(dairy, 1, "Nabiał"));
        readModelProjector.project(new ProductCategoryChanged(organic, 1, "Eko"));
        readModelProjector.project(new ProductDictChanged(milk, 1, "Mleko", List.of(), List.of(dairy, organic)));
        readModelProjector.project(new ProductDictChanged(cheese, 1, "Ser", List.of(), List.of(dairy)));

        readModelProjector.project(new ProductCategoryChanged(dairy, 2, "Produkty mleczne"));

        assertThat(categoryNamesByProduct()).containsOnly(
            Map.entry(milk, Set.of("Produkty mleczne", "Eko")),
            Map.entry(cheese, Set.of("Produkty mleczne")));
    }

    @Test
    void productProjectedBeforeItsCategoryGetsCategoryNameWhenCategoryArrives() {
        UUID dairy = UUID.randomUUID();
        UUID milk = UUID.randomUUID();
        readModelProjector.project(new ProductDictChanged(milk, 1, "Mleko", List.of(), List.of(dairy)));
        assertThat(categoryNamesByProduct()).containsOnly(Map.entry(milk, Set.of()));

        readModelProjector.project(new ProductCategoryChanged(dairy, 1, "Nabiał"));

        assertThat(categoryNamesByProduct()).containsOnly(Map.entry(milk, Set.of("Nabiał")));
    }

    private Map<UUID, Set<String>> categoryNamesByProduct() {
        return productDictQueryRepository.findAllWithCategoryAndAliases().stream()
            .collect(Collectors.toMap(ProductDictDto::id, product -> product.categories().stream()
                .map(ProductDictDto.Category::name)
                .collect(Collectors.toSet())));
    }
}
