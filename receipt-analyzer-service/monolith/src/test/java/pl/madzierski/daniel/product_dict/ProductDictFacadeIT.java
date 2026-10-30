package pl.madzierski.daniel.product_dict;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.madzierski.daniel.IntegrationTest;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.product_dict.model.CreateProductCategoryRequest;
import pl.madzierski.daniel.product_dict.model.GetProductCategoryListResponse;
import pl.madzierski.daniel.product_dict.model.GetProductDictListResponse;
import pl.madzierski.daniel.product_dict.model.UpdateProductCategoryRequest;
import pl.madzierski.daniel.product_dict.model.UpdateProductDictListRequest;
import pl.madzierski.daniel.product_dict.model.UpdateProductDictListRequest.UpdateProductDict;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProductDictFacadeIT extends IntegrationTest {

    private static final String USER_SUB = "test-user";

    @Autowired
    private ProductDictFacade productDictFacade;

    @BeforeEach
    void createUser() {
        createUser(USER_SUB);
    }

    @Test
    void createsCategoryWithTrimmedName() {
        productDictFacade.addProductCategory(USER_SUB, new CreateProductCategoryRequest("  Warzywa "));
        flush();

        assertThat(productDictFacade.getProductCategoryList().items())
            .extracting(GetProductCategoryListResponse.ProductCategory::name)
            .containsExactly("Warzywa");
    }

    @Test
    void rejectsCategoryThatDiffersOnlyInLetterCase() {
        productDictFacade.addProductCategory(USER_SUB, new CreateProductCategoryRequest("Warzywa"));
        flush();

        assertError(() -> productDictFacade.addProductCategory(USER_SUB, new CreateProductCategoryRequest("WARZYWA")),
            AppRuntimeExceptionMessages.PRODUCT_CATEGORY_ALREADY_EXISTS);
    }

    @Test
    void renamesCategoryButNotToNameOfAnotherCategory() {
        String vegetablesId = productDictFacade.addProductCategory(USER_SUB, new CreateProductCategoryRequest("Warzywa")).id();
        productDictFacade.addProductCategory(USER_SUB, new CreateProductCategoryRequest("Owoce"));
        flush();

        productDictFacade.updateProductCategory(vegetablesId, new UpdateProductCategoryRequest("warzywa"));
        flush();

        assertThat(productDictFacade.getProductCategoryList().items())
            .extracting(GetProductCategoryListResponse.ProductCategory::name)
            .containsExactly("Owoce", "warzywa");
        assertError(() -> productDictFacade.updateProductCategory(vegetablesId, new UpdateProductCategoryRequest("owoce")),
            AppRuntimeExceptionMessages.PRODUCT_CATEGORY_ALREADY_EXISTS);
    }

    @Test
    void categoryUsedByProductCannotBeDeleted() {
        String usedId = productDictFacade.addProductCategory(USER_SUB, new CreateProductCategoryRequest("Warzywa")).id();
        String unusedId = productDictFacade.addProductCategory(USER_SUB, new CreateProductCategoryRequest("Owoce")).id();
        flush();
        assignCategory(insertProduct("Cebula"), usedId);

        assertError(() -> productDictFacade.deleteProductCategory(usedId), AppRuntimeExceptionMessages.PRODUCT_CATEGORY_IN_USE);

        productDictFacade.deleteProductCategory(unusedId);
        flush();
        assertThat(productDictFacade.getProductCategoryList().items())
            .extracting(GetProductCategoryListResponse.ProductCategory::name)
            .containsExactly("Warzywa");
    }

    @Test
    void mergingProductsKeepsChosenAliasesAndMovesItemsAndCategoriesToPrimaryProduct() {
        String categoryId = productDictFacade.addProductCategory(USER_SUB, new CreateProductCategoryRequest("Nabiał")).id();
        String primaryId = insertProduct("Mleko");
        String primaryAliasId = insertAlias(primaryId, "Mleko 3,2% 1l");
        String secondId = insertProduct("Mleko łaciate");
        String keptAliasId = insertAlias(secondId, "MlekoŁaciate1l");
        String droppedAliasId = insertAlias(secondId, "Mleko typo");
        String thirdId = insertProduct("Mleko UHT");
        String thirdAliasId = insertAlias(thirdId, "MlekoUHT2%");
        String revisionId = insertRevision(insertReceipt(null, null, "Receipt"), "USER", "10.00", true, null);
        String secondItemId = insertItem(revisionId, "MlekoŁaciate1l", secondId);
        String thirdItemId = insertItem(revisionId, "MlekoUHT2%", thirdId);
        flush();

        productDictFacade.updateProductDict(new UpdateProductDictListRequest(List.of(new UpdateProductDict(
            "Mleko", List.of(categoryId), List.of(primaryId, secondId, thirdId), Set.of(primaryAliasId, keptAliasId, thirdAliasId)))));
        flush();

        assertThat(count("SELECT count(*) FROM product WHERE id IN (?, ?)", secondId, thirdId)).isZero();
        assertThat(count("SELECT count(*) FROM product_alias WHERE id = ?", droppedAliasId)).isZero();
        assertThat(jdbcTemplate.queryForList("SELECT product_id FROM receipt_item WHERE id IN (?, ?)", String.class, secondItemId, thirdItemId))
            .containsOnly(primaryId);
        GetProductDictListResponse.ProductDict merged = productDictFacade.getProductDictList().items().stream()
            .filter(product -> product.id().equals(primaryId))
            .findFirst()
            .orElseThrow();
        assertThat(merged.name()).isEqualTo("Mleko");
        assertThat(merged.aliases()).extracting(GetProductDictListResponse.ProductDict.Alias::alias)
            .containsExactlyInAnyOrder("Mleko", "Mleko 3,2% 1l", "MlekoŁaciate1l", "MlekoUHT2%");
        assertThat(merged.productCategories()).extracting(GetProductDictListResponse.ProductDict.ProductCategory::name)
            .containsExactly("Nabiał");
    }

    @Test
    void mergingIntoUnknownProductFails() {
        UpdateProductDictListRequest request = new UpdateProductDictListRequest(List.of(new UpdateProductDict(
            "Cebula", List.of(), List.of(UNKNOWN_ID), Set.of())));

        assertError(() -> productDictFacade.updateProductDict(request), AppRuntimeExceptionMessages.PRODUCT_NOT_FOUND);
    }

    @Test
    void unknownCategoryIsReported() {
        String productId = insertProduct("Cebula");

        assertError(() -> productDictFacade.updateProductCategory(UNKNOWN_ID, new UpdateProductCategoryRequest("Warzywa")),
            AppRuntimeExceptionMessages.PRODUCT_CATEGORY_NOT_FOUND);
        assertError(() -> productDictFacade.deleteProductCategory(UNKNOWN_ID),
            AppRuntimeExceptionMessages.PRODUCT_CATEGORY_NOT_FOUND);
        assertError(() -> productDictFacade.updateProductDict(new UpdateProductDictListRequest(List.of(
                new UpdateProductDict("Cebula", List.of(UNKNOWN_ID), List.of(productId), Set.of())))),
            AppRuntimeExceptionMessages.PRODUCT_CATEGORY_NOT_FOUND);
    }

    @Test
    void canonicalNameMatchingExistingAliasDoesNotDuplicateIt() {
        String productId = insertProduct("Cebula biała");
        String aliasId = insertAlias(productId, "cebula BIAŁA");
        flush();

        productDictFacade.updateProductDict(new UpdateProductDictListRequest(List.of(new UpdateProductDict(
            "Cebula Biała", List.of(), List.of(productId), Set.of(aliasId)))));
        flush();

        assertThat(jdbcTemplate.queryForObject("SELECT name FROM product WHERE id = ?", String.class, productId)).isEqualTo("Cebula Biała");
        assertThat(aliasesOf(productId)).containsExactly("cebula BIAŁA");
    }

    @Test
    void updatingProductReplacesItsCategories() {
        String vegetablesId = productDictFacade.addProductCategory(USER_SUB, new CreateProductCategoryRequest("Warzywa")).id();
        String organicId = productDictFacade.addProductCategory(USER_SUB, new CreateProductCategoryRequest("Eko")).id();
        flush();
        String productId = insertProduct("Cebula");
        String aliasId = insertAlias(productId, "Cebula");
        assignCategory(productId, vegetablesId);
        flush();

        productDictFacade.updateProductDict(new UpdateProductDictListRequest(List.of(new UpdateProductDict(
            "Cebula", List.of(organicId), List.of(productId), Set.of(aliasId)))));
        flush();

        assertThat(jdbcTemplate.queryForList(
            "SELECT c.name FROM product_category_mapping m JOIN product_category c ON c.id = m.category_id WHERE m.product_id = ?",
            String.class, productId)).containsExactly("Eko");
    }

    private Set<String> aliasesOf(String productId) {
        return Set.copyOf(jdbcTemplate.queryForList("SELECT name FROM product_alias WHERE product_id = ?", String.class, productId));
    }

    private void assignCategory(String productId, String categoryId) {
        jdbcTemplate.update("INSERT INTO product_category_mapping (product_id, category_id) VALUES (?, ?)", productId, categoryId);
    }

}
