package pl.madzierski.daniel.product_dict;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.madzierski.daniel.IntegrationTest;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.product_dict.model.AliasToLearn;
import pl.madzierski.daniel.product_dict.model.CreateProductCategoryRequest;
import pl.madzierski.daniel.product_dict.model.GetProductCategoryListResponse;
import pl.madzierski.daniel.product_dict.model.GetProductDictListResponse;
import pl.madzierski.daniel.product_dict.model.ProductDictQuery;
import pl.madzierski.daniel.product_dict.model.UpdateProductCategoryRequest;
import pl.madzierski.daniel.product_dict.model.UpdateProductDictListRequest;
import pl.madzierski.daniel.product_dict.model.UpdateProductDictListRequest.UpdateProductDict;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
        productDictFacade.addProductCategory(new CreateProductCategoryRequest("  Warzywa "));
        flush();

        assertThat(productDictFacade.getProductCategoryList().items())
            .extracting(GetProductCategoryListResponse.ProductCategory::name)
            .containsExactly("Warzywa");
    }

    @Test
    void rejectsCategoryThatDiffersOnlyInLetterCase() {
        productDictFacade.addProductCategory(new CreateProductCategoryRequest("Warzywa"));
        flush();

        assertError(() -> productDictFacade.addProductCategory(new CreateProductCategoryRequest("WARZYWA")),
            AppRuntimeExceptionMessages.PRODUCT_CATEGORY_ALREADY_EXISTS);
    }

    @Test
    void updatingCategoryWithStaleVersionFails() {
        UUID vegetablesId = productDictFacade.addProductCategory(new CreateProductCategoryRequest("Warzywa")).id();
        long staleVersion = version("product_category", vegetablesId);
        productDictFacade.updateProductCategory(vegetablesId, new UpdateProductCategoryRequest(staleVersion, "Owoce"));
        flush();

        assertError(() -> productDictFacade.updateProductCategory(vegetablesId, new UpdateProductCategoryRequest(staleVersion, "Zioła")),
            AppRuntimeExceptionMessages.VERSION_CONFLICT);
    }

    @Test
    void updatingProductWithStaleVersionFails() {
        UUID productId = insertProduct("Cebula");
        UUID aliasId = insertAlias(productId, "Cebula");
        flush();
        UpdateProductDictListRequest stale = new UpdateProductDictListRequest(List.of(new UpdateProductDict(
            version("product", productId), "Cebula biała", List.of(), List.of(productId), Set.of(aliasId))));
        productDictFacade.updateProductDict(stale);
        flush();

        assertError(() -> productDictFacade.updateProductDict(stale), AppRuntimeExceptionMessages.VERSION_CONFLICT);
    }

    @Test
    void renamesCategoryButNotToNameOfAnotherCategory() {
        UUID vegetablesId = productDictFacade.addProductCategory(new CreateProductCategoryRequest("Warzywa")).id();
        productDictFacade.addProductCategory(new CreateProductCategoryRequest("Owoce"));
        flush();

        productDictFacade.updateProductCategory(vegetablesId, new UpdateProductCategoryRequest(version("product_category", vegetablesId), "warzywa"));
        flush();

        assertThat(productDictFacade.getProductCategoryList().items())
            .extracting(GetProductCategoryListResponse.ProductCategory::name)
            .containsExactly("Owoce", "warzywa");
        assertError(() -> productDictFacade.updateProductCategory(vegetablesId, new UpdateProductCategoryRequest(version("product_category", vegetablesId), "owoce")),
            AppRuntimeExceptionMessages.PRODUCT_CATEGORY_ALREADY_EXISTS);
    }

    @Test
    void categoryUsedByProductCannotBeDeleted() {
        UUID usedId = productDictFacade.addProductCategory(new CreateProductCategoryRequest("Warzywa")).id();
        UUID unusedId = productDictFacade.addProductCategory(new CreateProductCategoryRequest("Owoce")).id();
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
        UUID categoryId = productDictFacade.addProductCategory(new CreateProductCategoryRequest("Nabiał")).id();
        UUID primaryId = insertProduct("Mleko");
        UUID primaryAliasId = insertAlias(primaryId, "Mleko 3,2% 1l");
        UUID secondId = insertProduct("Mleko łaciate");
        UUID keptAliasId = insertAlias(secondId, "MlekoŁaciate1l");
        UUID droppedAliasId = insertAlias(secondId, "Mleko typo");
        UUID thirdId = insertProduct("Mleko UHT");
        UUID thirdAliasId = insertAlias(thirdId, "MlekoUHT2%");
        UUID userId = jdbcTemplate.queryForObject("SELECT id FROM app_user WHERE user_sub = ?", UUID.class, USER_SUB);
        UUID revisionId = insertRevision(insertReceipt(createWallet(userId, "Home"), null, "Receipt"), "USER", "10.00", true, null);
        UUID secondItemId = insertItem(revisionId, "MlekoŁaciate1l", secondId);
        UUID thirdItemId = insertItem(revisionId, "MlekoUHT2%", thirdId);
        flush();

        productDictFacade.updateProductDict(new UpdateProductDictListRequest(List.of(new UpdateProductDict(
            version("product", primaryId), "Mleko", List.of(categoryId), List.of(primaryId, secondId, thirdId), Set.of(primaryAliasId, keptAliasId, thirdAliasId)))));
        flush();

        assertThat(count("SELECT count(*) FROM product WHERE id IN (?, ?)", secondId, thirdId)).isZero();
        assertThat(count("SELECT count(*) FROM product_alias WHERE id = ?", droppedAliasId)).isZero();
        assertThat(jdbcTemplate.queryForList("SELECT product_id FROM receipt_item WHERE id IN (?, ?)", UUID.class, secondItemId, thirdItemId))
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
            version("product", UNKNOWN_ID), "Cebula", List.of(), List.of(UNKNOWN_ID), Set.of())));

        assertError(() -> productDictFacade.updateProductDict(request), AppRuntimeExceptionMessages.PRODUCT_NOT_FOUND);
    }

    @Test
    void unknownCategoryIsReported() {
        UUID productId = insertProduct("Cebula");

        assertError(() -> productDictFacade.updateProductCategory(UNKNOWN_ID, new UpdateProductCategoryRequest(version("product_category", UNKNOWN_ID), "Warzywa")),
            AppRuntimeExceptionMessages.PRODUCT_CATEGORY_NOT_FOUND);
        assertError(() -> productDictFacade.deleteProductCategory(UNKNOWN_ID),
            AppRuntimeExceptionMessages.PRODUCT_CATEGORY_NOT_FOUND);
        assertError(() -> productDictFacade.updateProductDict(new UpdateProductDictListRequest(List.of(
                new UpdateProductDict(version("product", productId), "Cebula", List.of(UNKNOWN_ID), List.of(productId), Set.of())))),
            AppRuntimeExceptionMessages.PRODUCT_CATEGORY_NOT_FOUND);
    }

    @Test
    void canonicalNameMatchingExistingAliasDoesNotDuplicateIt() {
        UUID productId = insertProduct("Cebula biała");
        UUID aliasId = insertAlias(productId, "cebula BIAŁA");
        flush();

        productDictFacade.updateProductDict(new UpdateProductDictListRequest(List.of(new UpdateProductDict(
            version("product", productId), "Cebula Biała", List.of(), List.of(productId), Set.of(aliasId)))));
        flush();

        assertThat(jdbcTemplate.queryForObject("SELECT name FROM product WHERE id = ?", String.class, productId)).isEqualTo("Cebula Biała");
        assertThat(aliasesOf(productId)).containsExactly("cebula BIAŁA");
    }

    @Test
    void updatingProductReplacesItsCategories() {
        UUID vegetablesId = productDictFacade.addProductCategory(new CreateProductCategoryRequest("Warzywa")).id();
        UUID organicId = productDictFacade.addProductCategory(new CreateProductCategoryRequest("Eko")).id();
        flush();
        UUID productId = insertProduct("Cebula");
        UUID aliasId = insertAlias(productId, "Cebula");
        assignCategory(productId, vegetablesId);
        flush();

        productDictFacade.updateProductDict(new UpdateProductDictListRequest(List.of(new UpdateProductDict(
            version("product", productId), "Cebula", List.of(organicId), List.of(productId), Set.of(aliasId)))));
        flush();

        assertThat(jdbcTemplate.queryForList(
            "SELECT c.name FROM product_category_mapping m JOIN product_category c ON c.id = m.category_id WHERE m.product_id = ?",
            String.class, productId)).containsExactly("Eko");
    }

    @Test
    void eachProductGroupInOneRequestIsMergedSeparately() {
        UUID milkId = insertProduct("Mleko");
        UUID milkAliasId = insertAlias(milkId, "Mleko");
        UUID otherMilkId = insertProduct("Mleko 2");
        UUID otherMilkAliasId = insertAlias(otherMilkId, "MLK 3,2");
        UUID breadId = insertProduct("Chleb");
        UUID breadAliasId = insertAlias(breadId, "Chleb");
        UUID otherBreadId = insertProduct("Chleb 2");
        UUID otherBreadAliasId = insertAlias(otherBreadId, "CHL ZYT");
        UUID untouchedId = insertProduct("Cebula");
        UUID userId = jdbcTemplate.queryForObject("SELECT id FROM app_user WHERE user_sub = ?", UUID.class, USER_SUB);
        UUID revisionId = insertRevision(insertReceipt(createWallet(userId, "Home"), null, "Receipt"), "USER", "10.00", true, null);
        UUID milkItemId = insertItem(revisionId, "MLK 3,2", otherMilkId);
        UUID breadItemId = insertItem(revisionId, "CHL ZYT", otherBreadId);
        UUID onionItemId = insertItem(revisionId, "CEB", untouchedId);
        flush();

        productDictFacade.updateProductDict(new UpdateProductDictListRequest(List.of(
            new UpdateProductDict(version("product", milkId), "Mleko", List.of(), List.of(milkId, otherMilkId), Set.of(milkAliasId, otherMilkAliasId)),
            new UpdateProductDict(version("product", breadId), "Chleb", List.of(), List.of(breadId, otherBreadId), Set.of(breadAliasId, otherBreadAliasId)))));
        flush();

        assertThat(jdbcTemplate.queryForList("SELECT name FROM product", String.class)).containsExactlyInAnyOrder("Mleko", "Chleb", "Cebula");
        assertThat(aliasesOf(milkId)).containsExactlyInAnyOrder("Mleko", "MLK 3,2");
        assertThat(aliasesOf(breadId)).containsExactlyInAnyOrder("Chleb", "CHL ZYT");
        assertThat(productOfItem(milkItemId)).isEqualTo(milkId);
        assertThat(productOfItem(breadItemId)).isEqualTo(breadId);
        assertThat(productOfItem(onionItemId)).isEqualTo(untouchedId);
    }

    @Test
    void renamingProductAddsNewCanonicalAliasAndDropsAliasesNotChosen() {
        UUID productId = insertProduct("Cebul");
        insertAlias(productId, "Cebul");
        UUID keptAliasId = insertAlias(productId, "CEB ZOL");
        flush();

        productDictFacade.updateProductDict(new UpdateProductDictListRequest(List.of(new UpdateProductDict(
            version("product", productId), "  Cebula ", List.of(), List.of(productId), Set.of(keptAliasId)))));
        flush();

        assertThat(jdbcTemplate.queryForObject("SELECT name FROM product WHERE id = ?", String.class, productId)).isEqualTo("Cebula");
        assertThat(aliasesOf(productId)).containsExactlyInAnyOrder("Cebula", "CEB ZOL");
    }

    @Test
    void correctionToNameOfExistingProductWithoutThatAliasJoinsThatProduct() {
        UUID onionId = insertProduct("Cebula");
        AliasToLearn red = new AliasToLearn("Cebula Czerwona Luz", "cebula", null);
        AliasToLearn white = new AliasToLearn("Cebula Biala Luz", "Cebula", null);

        Map<AliasToLearn, ProductDictQuery> learned = productDictFacade.learnAliases(List.of(red, white));
        flush();

        assertThat(learned).containsEntry(red, new ProductDictQuery(onionId)).containsEntry(white, new ProductDictQuery(onionId));
        assertThat(aliasesOf(onionId)).contains("Cebula Czerwona Luz", "Cebula Biala Luz");
        assertThat(count("SELECT count(*) FROM product WHERE LOWER(name) = 'cebula'")).isEqualTo(1);
    }

    private UUID productOfItem(UUID itemId) {
        return jdbcTemplate.queryForObject("SELECT product_id FROM receipt_item WHERE id = ?", UUID.class, itemId);
    }

    private Set<String> aliasesOf(UUID productId) {
        return Set.copyOf(jdbcTemplate.queryForList("SELECT name FROM product_alias WHERE product_id = ?", String.class, productId));
    }

    private void assignCategory(UUID productId, UUID categoryId) {
        jdbcTemplate.update("INSERT INTO product_category_mapping (product_id, category_id) VALUES (?, ?)", productId, categoryId);
    }

}
