package pl.madzierski.daniel.receipt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import pl.madzierski.daniel.IntegrationTest;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.file_group.FileFacade;
import pl.madzierski.daniel.receipt.model.CreateReceiptRequest;
import pl.madzierski.daniel.receipt.model.CreateRevisionRequest;
import pl.madzierski.daniel.receipt.model.GetReceiptDetailsResponse;
import pl.madzierski.daniel.receipt.model.GetRevisionResponse;
import pl.madzierski.daniel.receipt.model.UpdateReceiptRequest;
import pl.madzierski.daniel.receipt.model.UpdateRevisionRequest;
import pl.madzierski.daniel.receipt.model.UpdateRevisionRequest.ItemRequest;
import pl.madzierski.daniel.store.model.StoreDetailsResponse;
import pl.madzierski.daniel.user.model.AuthenticatedUser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class ReceiptFacadeIT extends IntegrationTest {

    private static final String USER_SUB = "test-user";
    private static final String RECEIPT_JSON = "receipts/biedronka/biedronka_e_receipt.json";
    private static final String ONION = "Cebula Żółta Luz";
    private static final String CHICKEN = "Fil  Z Piersi K kg";
    private static final String STRANGER_SUB = "stranger";

    @Autowired
    private ReceiptFacade receiptFacade;

    @Autowired
    private FileFacade fileFacade;

    private UUID walletId;
    private UUID storeId;
    private AuthenticatedUser user;

    @BeforeEach
    void createOwnerWalletAndStore() {
        UUID userId = createUser(USER_SUB);
        user = new AuthenticatedUser(userId);
        walletId = createWallet(userId, "Test wallet");
        storeId = createStore("Biedronka", "ul. Testowa 1", "Poznań");
    }

    @Test
    void createsReceiptWithRevisionFromBiedronkaJson() {
        UUID receiptId = createReceiptFromJson();

        Map<String, Object> revision = jdbcTemplate.queryForMap(
            "SELECT id, resolver, total_price, is_correct FROM receipt_revision WHERE receipt_id = ?", receiptId);
        assertThat(revision.get("resolver")).isEqualTo("BIEDRONKA_JSON");
        assertThat((BigDecimal) revision.get("total_price")).isEqualByComparingTo("330.75");
        assertThat(revision.get("is_correct")).isEqualTo(true);
        assertThat(itemsOf((UUID) revision.get("id"))).hasSize(36);
    }

    @Test
    void storesUploadedFileInUploadDirectory() throws IOException {
        UUID receiptId = createReceiptFromJson();

        List<String> paths = jdbcTemplate.queryForList(
            "SELECT f.path FROM receipt_file f JOIN receipt_file_group g ON g.id = f.file_group_id WHERE g.receipt_id = ?",
            String.class, receiptId);
        assertThat(paths).hasSize(1);
        Path stored = Path.of(paths.getFirst());
        assertThat(stored).startsWith(uploadDir()).exists();
        assertThat(Files.readAllBytes(stored)).isEqualTo(readResource(RECEIPT_JSON));
    }

    @Test
    void removingItemAndAddingNewOneInSameSaveDeletesRemovedItem() {
        UUID receiptId = createReceiptFromJson();
        UUID userRevisionId = copyRevision(revisionIdOf(receiptId));

        List<ItemRequest> items = itemsOf(userRevisionId);
        ItemRequest removed = items.getFirst();
        Set<ItemRequest> request = new HashSet<>(items.subList(1, items.size()));
        request.add(new ItemRequest(null, removed.name(), removed.amount(), removed.unitPrice(), removed.discount(),
            removed.totalPrice(), removed.position(), null));

        receiptFacade.updateRevision(user, userRevisionId, revisionUpdate(userRevisionId, request));
        flush();

        Set<UUID> itemIds = itemsOf(userRevisionId).stream().map(ItemRequest::id).collect(Collectors.toSet());
        assertThat(itemIds).hasSize(36).doesNotContain(removed.id());
        assertThat(itemsOf(userRevisionId)).extracting(ItemRequest::name).contains(removed.name());
    }

    @Test
    void updatingReceiptKeepsItsWallet() {
        UUID receiptId = createReceiptFromJson();
        UUID otherStoreId = createStore("Lidl", "ul. Inna 2", "Kraków");

        receiptFacade.updateReceipt(user, receiptId, new UpdateReceiptRequest(version("receipt", receiptId), "  Weekly shopping ", "groceries", otherStoreId));
        flush();

        Map<String, Object> receipt = jdbcTemplate.queryForMap(
            "SELECT name, description, wallet_id, store_id FROM receipt WHERE id = ?", receiptId);
        assertThat(receipt.get("name")).isEqualTo("Weekly shopping");
        assertThat(receipt.get("description")).isEqualTo("groceries");
        assertThat(receipt.get("wallet_id")).isEqualTo(walletId);
        assertThat(receipt.get("store_id")).isEqualTo(otherStoreId);
    }

    @Test
    void updatingReceiptWithStaleVersionFailsAndKeepsNewerData() {
        UUID receiptId = createReceiptFromJson();
        long staleVersion = version("receipt", receiptId);
        receiptFacade.updateReceipt(user, receiptId, new UpdateReceiptRequest(staleVersion, "First", null, storeId));
        flush();

        assertError(() -> receiptFacade.updateReceipt(user, receiptId, new UpdateReceiptRequest(staleVersion, "Second", null, storeId)),
            AppRuntimeExceptionMessages.VERSION_CONFLICT);
        assertThat(jdbcTemplate.queryForObject("SELECT name FROM receipt WHERE id = ?", String.class, receiptId)).isEqualTo("First");
    }

    @Test
    void updatingRevisionWithStaleVersionFails() {
        UUID copyId = copyRevision(revisionIdOf(createReceiptFromJson()));
        UpdateRevisionRequest stale = summaryUpdate(copyId, new BigDecimal("1.00"), null, null, null);
        receiptFacade.updateRevision(user, copyId, stale);
        flush();

        assertError(() -> receiptFacade.updateRevision(user, copyId, stale), AppRuntimeExceptionMessages.VERSION_CONFLICT);
    }

    @Test
    void editingOnlyItemsOfRevisionIncreasesItsVersion() {
        UUID copyId = copyRevision(revisionIdOf(createReceiptFromJson()));
        GetRevisionResponse revision = receiptFacade.getRevision(user, copyId);
        Set<ItemRequest> items = itemsOf(copyId).stream()
            .map(item -> item.name().equals(ONION)
                ? new ItemRequest(item.id(), "Cebula", item.amount(), item.unitPrice(), item.discount(), item.totalPrice(), item.position(), item.productId())
                : item)
            .collect(Collectors.toSet());

        receiptFacade.updateRevision(user, copyId, new UpdateRevisionRequest(null, revision.version(), null, null,
            revision.totalPrice(), revision.paymentDate(), null, revision.isCorrect(), items));

        assertThat(version("receipt_revision", copyId)).isEqualTo(revision.version() + 1);
    }

    @Test
    void updatingReceiptWithUnknownStoreFails() {
        UUID receiptId = createReceiptFromJson();

        assertError(() -> receiptFacade.updateReceipt(user, receiptId, new UpdateReceiptRequest(version("receipt", receiptId), "name", null, UNKNOWN_ID)),
            AppRuntimeExceptionMessages.STORE_NOT_FOUND);
    }

    interface FacadeCall {
        void call(ReceiptFacade facade, AuthenticatedUser user, UUID id);
    }

    static Stream<Named<FacadeCall>> receiptCalls() {
        return Stream.of(
            Named.of("getReceiptDetails", (facade, user, id) -> facade.getReceiptDetails(user, id)),
            Named.of("createRevision", (facade, user, id) -> facade.createRevision(user, id, new CreateRevisionRequest(UNKNOWN_ID, ReceiptResolverStrategyType.USER))),
            Named.of("updateReceipt", (facade, user, id) -> facade.updateReceipt(user, id, new UpdateReceiptRequest(0L, "name", null, UNKNOWN_ID))),
            Named.of("deleteReceipt", (facade, user, id) -> facade.deleteReceipt(user, id)));
    }

    @ParameterizedTest
    @MethodSource("receiptCalls")
    void unknownReceiptIsReported(FacadeCall call) {
        assertError(() -> call.call(receiptFacade, user, UNKNOWN_ID), AppRuntimeExceptionMessages.RECEIPT_NOT_FOUND);
    }

    @ParameterizedTest
    @MethodSource("receiptCalls")
    void otherUsersReceiptIsReportedAsUnknownAndStaysUntouched(FacadeCall call) {
        UUID receiptId = createReceiptFromJson();
        AuthenticatedUser stranger = new AuthenticatedUser(createUser(STRANGER_SUB));

        assertError(() -> call.call(receiptFacade, stranger, receiptId), AppRuntimeExceptionMessages.RECEIPT_NOT_FOUND);
        flush();

        assertThat(jdbcTemplate.queryForObject("SELECT name FROM receipt WHERE id = ?", String.class, receiptId)).isEqualTo("Biedronka 22.05");
        assertThat(count("SELECT count(*) FROM receipt_revision WHERE receipt_id = ?", receiptId)).isEqualTo(1);
        assertThat(count("SELECT count(*) FROM receipt_file_group WHERE receipt_id = ?", receiptId)).isEqualTo(1);
    }

    @Test
    void otherUsersReceiptHasNoVisibleRevisions() {
        UUID receiptId = createReceiptFromJson();
        AuthenticatedUser stranger = new AuthenticatedUser(createUser(STRANGER_SUB));

        assertThat(receiptFacade.getReceiptRevisions(stranger, receiptId)).isEmpty();
        assertThat(receiptFacade.getReceiptRevisions(user, receiptId)).hasSize(1);
    }

    @Test
    void receiptCannotBeAddedToOtherUsersWallet() {
        UUID strangerWalletId = createWallet(createUser(STRANGER_SUB), "Stranger's wallet");

        assertError(() -> receiptFacade.addReceipt(user, List.of(readResource(RECEIPT_JSON)),
                new CreateReceiptRequest("Biedronka 22.05", null, ReceiptResolverStrategyType.BIEDRONKA_JSON, strangerWalletId, storeId)),
            AppRuntimeExceptionMessages.WALLET_NOT_FOUND);
        flush();

        assertThat(count("SELECT count(*) FROM receipt WHERE wallet_id = ?", strangerWalletId)).isZero();
    }

    @Test
    void revisionCannotBeResolvedFromFileGroupOfOtherUsersReceipt() {
        UUID receiptId = createReceiptFromJson();
        UUID strangerId = createUser(STRANGER_SUB);
        UUID strangerReceiptId = receiptFacade.addReceipt(new AuthenticatedUser(strangerId), List.of(readResource(RECEIPT_JSON)),
            new CreateReceiptRequest("Stranger's receipt", null, ReceiptResolverStrategyType.BIEDRONKA_JSON,
                createWallet(strangerId, "Stranger's wallet"), storeId)).id();
        flush();
        UUID strangerFileGroupId = jdbcTemplate.queryForObject("SELECT id FROM receipt_file_group WHERE receipt_id = ?", UUID.class, strangerReceiptId);

        assertError(() -> receiptFacade.createRevision(user, receiptId, new CreateRevisionRequest(strangerFileGroupId, ReceiptResolverStrategyType.BIEDRONKA_JSON)),
            AppRuntimeExceptionMessages.FILE_GROUP_NOT_FOUND);
    }

    @Test
    void deletingReceiptRemovesRevisionsItemsAndFileGroupsUsedByThem() {
        UUID receiptId = createReceiptFromJson();
        UUID originalId = revisionIdOf(receiptId);
        UUID copyId = copyRevision(originalId);
        UUID fileGroupId = jdbcTemplate.queryForObject("SELECT source_file_group_id FROM receipt_revision WHERE id = ?", UUID.class, copyId);
        assertThat(fileGroupId).isNotNull();

        receiptFacade.deleteReceipt(user, receiptId);
        flush();

        assertThat(count("SELECT count(*) FROM receipt WHERE id = ?", receiptId)).isZero();
        assertThat(count("SELECT count(*) FROM receipt_revision WHERE id IN (?, ?)", originalId, copyId)).isZero();
        assertThat(count("SELECT count(*) FROM receipt_item WHERE receipt_revision_id IN (?, ?)", originalId, copyId)).isZero();
        assertThat(count("SELECT count(*) FROM receipt_file_group WHERE id = ?", fileGroupId)).isZero();
        assertThat(count("SELECT count(*) FROM receipt_file WHERE file_group_id = ?", fileGroupId)).isZero();
    }

    @Test
    void copyIsEditableUserRevisionLinkedToOriginal() {
        UUID originalId = revisionIdOf(createReceiptFromJson());

        UUID copyId = copyRevision(originalId);

        Map<String, Object> copy = jdbcTemplate.queryForMap(
            "SELECT c.resolver, c.is_correct, c.total_price, c.parent_receipt_revision_id, "
                + "c.source_file_group_id = o.source_file_group_id AS same_source "
                + "FROM receipt_revision c JOIN receipt_revision o ON o.id = ? WHERE c.id = ?", originalId, copyId);
        assertThat(copy.get("resolver")).isEqualTo("USER");
        assertThat(isPreferred(copyId)).isFalse();
        assertThat(copy.get("is_correct")).isEqualTo(true);
        assertThat(copy.get("parent_receipt_revision_id")).isEqualTo(originalId);
        assertThat(copy.get("same_source")).isEqualTo(true);
        assertThat(itemsOf(copyId)).hasSize(36);
        assertThat(count(
            "SELECT count(*) FROM receipt_item c JOIN receipt_item o ON o.id = c.parent_item_id "
                + "WHERE c.receipt_revision_id = ? AND o.receipt_revision_id = ? "
                + "AND o.name = c.name AND o.amount = c.amount AND o.unit_price = c.unit_price AND o.total_price = c.total_price",
            copyId, originalId)).isEqualTo(36);
    }

    @Test
    void revisionWithCopiesCannotBeDeletedUntilCopiesAreGone() {
        UUID receiptId = createReceiptFromJson();
        UUID originalId = revisionIdOf(receiptId);
        UUID copyId = copyRevision(originalId);

        assertError(() -> receiptFacade.deleteRevision(user, originalId), AppRuntimeExceptionMessages.CAN_NOT_DELETE_REVISION_WITH_CHILD_REVISIONS);

        receiptFacade.deleteRevision(user, copyId);
        flush();
        receiptFacade.deleteRevision(user, originalId);
        flush();

        assertThat(count("SELECT count(*) FROM receipt_revision WHERE receipt_id = ?", receiptId)).isZero();
        assertThat(count("SELECT count(*) FROM receipt_item WHERE receipt_revision_id IN (?, ?)", originalId, copyId)).isZero();
    }

    @Test
    void userCorrectionBecomesProductWithOriginalNameAsAlias() {
        UUID originalId = revisionIdOf(createReceiptFromJson());
        UUID copyId = copyRevision(originalId);
        renameItem(copyId, ONION, "Cebula");

        receiptFacade.learnProductsFromCorrections(user, copyId);
        flush();

        assertThat(aliasesOf("Cebula")).containsExactlyInAnyOrder("Cebula", ONION);
        assertThat(productNameOfItem(itemNamed(originalId, ONION).id())).contains("Cebula");
        assertThat(nameShownInRevision(originalId, ONION)).isEqualTo("Cebula");
    }

    @Test
    void userCorrectionToExistingProductAddsAliasInsteadOfNewProduct() {
        UUID onionId = createProduct("Cebula", "Cebula");
        UUID originalId = revisionIdOf(createReceiptFromJson());
        UUID copyId = copyRevision(originalId);
        renameItem(copyId, ONION, "Cebula");

        receiptFacade.learnProductsFromCorrections(user, copyId);
        flush();

        assertThat(count("SELECT count(*) FROM product WHERE name = 'Cebula'")).isEqualTo(1);
        assertThat(aliasesOf("Cebula")).containsExactlyInAnyOrder("Cebula", ONION);
        assertThat(jdbcTemplate.queryForObject("SELECT product_id FROM receipt_item WHERE id = ?", UUID.class,
            itemNamed(originalId, ONION).id())).isEqualTo(onionId);
    }

    @Test
    void productChosenByUserLearnsOriginalNameAsAliasAndIsAssignedToOriginalItem() {
        UUID chosenId = createProduct("Kapusta", "Kapusta");
        UUID originalId = revisionIdOf(createReceiptFromJson());
        UUID copyId = copyRevision(originalId);
        setProductOfItem(copyId, ONION, chosenId);

        receiptFacade.learnProductsFromCorrections(user, copyId);
        flush();

        assertThat(aliasesOf("Kapusta")).containsExactlyInAnyOrder("Kapusta", ONION);
        assertThat(productIdsOfItemsNamed(originalId, ONION)).containsExactly(chosenId);
        assertThat(productIdsOfItemsNamed(copyId, ONION)).containsExactly(chosenId);
        assertThat(count("SELECT count(*) FROM product_alias WHERE name = ?", ONION)).isEqualTo(1);
    }

    @Test
    void learnedProductIsAssignedToItemsOfBothRevisions() {
        UUID onionId = createProduct("Cebula", "Cebula");
        UUID originalId = revisionIdOf(createReceiptFromJson());
        UUID copyId = copyRevision(originalId);
        renameItem(copyId, ONION, "Cebula");

        receiptFacade.learnProductsFromCorrections(user, copyId);
        flush();

        assertThat(productIdsOfItemsNamed(originalId, ONION)).containsExactly(onionId);
        assertThat(productIdsOfItemsNamed(copyId, "Cebula")).containsExactly(onionId);
    }

    @Test
    void learningAliasKeepsCategoriesAndAliasesOfExistingProduct() {
        UUID onionId = createProduct("Cebula", "Cebula", "Cebula czerwona");
        UUID categoryId = jdbcTemplate.queryForObject(
            "INSERT INTO product_category (id, created_date, name) VALUES (gen_random_uuid(), now(), 'Warzywa') RETURNING id",
            UUID.class);
        jdbcTemplate.update("INSERT INTO product_category_mapping (category_id, product_id) VALUES (?, ?)", categoryId, onionId);
        UUID originalId = revisionIdOf(createReceiptFromJson());
        UUID copyId = copyRevision(originalId);
        renameItem(copyId, ONION, "Cebula");

        receiptFacade.learnProductsFromCorrections(user, copyId);
        flush();

        assertThat(aliasesOf("Cebula")).containsExactlyInAnyOrder("Cebula", "Cebula czerwona", ONION);
        assertThat(jdbcTemplate.queryForList("SELECT category_id FROM product_category_mapping WHERE product_id = ?",
            UUID.class, onionId)).containsExactly(categoryId);
    }

    @Test
    void repeatedItemCorrectedToExistingProductAddsAliasOnce() {
        UUID chickenId = createProduct("Kurczak", "Kurczak");
        UUID originalId = revisionIdOf(createReceiptFromJson());
        UUID copyId = copyRevision(originalId);
        renameItem(copyId, CHICKEN, "Kurczak");

        receiptFacade.learnProductsFromCorrections(user, copyId);
        flush();

        assertThat(aliasesOf("Kurczak")).containsExactlyInAnyOrder("Kurczak", CHICKEN);
        assertThat(itemsOf(originalId).stream()
            .filter(item -> item.name().equals(CHICKEN))
            .map(item -> jdbcTemplate.queryForObject("SELECT product_id FROM receipt_item WHERE id = ?", UUID.class, item.id()))
            .collect(Collectors.toSet())).containsExactly(chickenId);
    }

    @Test
    void dictionaryLearnedFromOneReceiptIsUsedForNextOne() {
        UUID firstCopyId = copyRevision(revisionIdOf(createReceiptFromJson()));
        renameItem(firstCopyId, ONION, "Cebula");
        receiptFacade.learnProductsFromCorrections(user, firstCopyId);
        flush();

        UUID secondRevisionId = revisionIdOf(createReceiptFromJson());
        UUID secondCopyId = copyRevision(secondRevisionId);
        receiptFacade.learnProductsFromCorrections(user, secondCopyId);
        flush();

        assertThat(count("SELECT count(*) FROM product")).isEqualTo(35);
        assertThat(count("SELECT count(*) FROM receipt_item WHERE receipt_revision_id = ? AND product_id IS NULL", secondRevisionId)).isZero();
        assertThat(nameShownInRevision(secondRevisionId, ONION)).isEqualTo("Cebula");
    }

    @Test
    void itemsWithSameNameShareOneProductAndRepeatedUpdateDoesNotDuplicateIt() {
        UUID originalId = revisionIdOf(createReceiptFromJson());
        UUID copyId = copyRevision(originalId);
        receiptFacade.learnProductsFromCorrections(user, copyId);
        flush();

        receiptFacade.learnProductsFromCorrections(user, copyId);
        flush();

        assertThat(count("SELECT count(*) FROM product")).isEqualTo(35);
        assertThat(count("SELECT count(*) FROM product_alias")).isEqualTo(35);
        Set<String> chickenProducts = itemsOf(originalId).stream()
            .filter(item -> item.name().equals(CHICKEN))
            .map(item -> productNameOfItem(item.id()).orElse(null))
            .collect(Collectors.toSet());
        assertThat(chickenProducts).containsExactly(CHICKEN);
        assertThat(aliasesOf(CHICKEN)).containsExactly(CHICKEN);
    }

    @Test
    void differentItemsCorrectedToSameTextShareOneNewProduct() {
        UUID originalId = revisionIdOf(createReceiptFromJson());
        UUID copyId = copyRevision(originalId);
        renameItem(copyId, ONION, "Warzywa");
        renameItem(copyId, CHICKEN, "Warzywa");

        receiptFacade.learnProductsFromCorrections(user, copyId);
        flush();

        assertThat(count("SELECT count(*) FROM product WHERE name = 'Warzywa'")).isEqualTo(1);
        assertThat(aliasesOf("Warzywa")).containsExactlyInAnyOrder("Warzywa", ONION, CHICKEN);
        assertThat(productIdsOfItemsNamed(originalId, ONION)).isEqualTo(productIdsOfItemsNamed(originalId, CHICKEN));
    }

    @Test
    void correctionsDifferingOnlyInLetterCaseShareOneNewProduct() {
        UUID originalId = revisionIdOf(createReceiptFromJson());
        UUID copyId = copyRevision(originalId);
        renameItem(copyId, ONION, "Cebula");
        renameItem(copyId, CHICKEN, "cebula");

        receiptFacade.learnProductsFromCorrections(user, copyId);
        flush();

        assertThat(count("SELECT count(*) FROM product WHERE LOWER(name) = 'cebula'")).isEqualTo(1);
        Set<UUID> onionProductIds = productIdsOfItemsNamed(originalId, ONION);
        assertThat(onionProductIds).hasSize(1).isEqualTo(productIdsOfItemsNamed(originalId, CHICKEN));
        Set<String> aliases = aliasesOfProduct(onionProductIds.iterator().next());
        assertThat(aliases).contains(ONION, CHICKEN);
        assertThat(aliases).filteredOn(alias -> alias.equalsIgnoreCase("cebula")).hasSize(1);
    }

    @Test
    void aliasDifferingOnlyInLetterCaseIsNotAddedToExistingProduct() {
        UUID originalId = revisionIdOf(createReceiptFromJson());
        UUID copyId = copyRevision(originalId);
        String lowerCaseOnion = ONION.toLowerCase();
        UUID onionId = createProduct("Cebula", lowerCaseOnion);
        renameItem(copyId, ONION, "Cebula");

        receiptFacade.learnProductsFromCorrections(user, copyId);
        flush();

        assertThat(aliasesOf("Cebula")).containsExactly(lowerCaseOnion);
        assertThat(productIdsOfItemsNamed(originalId, ONION)).containsExactly(onionId);
    }

    @Test
    void markingRevisionAsPreferredUnmarksOtherRevisionsOfReceipt() {
        UUID originalId = revisionIdOf(createReceiptFromJson());
        UUID copyId = copyRevision(originalId);
        receiptFacade.updateRevision(user, originalId, preferredUpdate(originalId));
        flush();

        receiptFacade.updateRevision(user, copyId, preferredUpdate(copyId));
        flush();

        assertThat(isPreferred(originalId)).isFalse();
        assertThat(isPreferred(copyId)).isTrue();
    }

    @Test
    void editWithInconsistentPricesDoesNotMarkRevisionAsCorrect() {
        UUID copyId = copyRevision(revisionIdOf(createReceiptFromJson()));
        jdbcTemplate.update("UPDATE receipt_revision SET is_correct = false WHERE id = ?", copyId);
        Set<ItemRequest> items = itemsOf(copyId).stream()
            .map(item -> item.name().equals(ONION)
                ? new ItemRequest(item.id(), item.name(), item.amount(), item.unitPrice(), item.discount(), item.totalPrice().add(BigDecimal.ONE), item.position(), item.productId())
                : item)
            .collect(Collectors.toSet());

        receiptFacade.updateRevision(user, copyId, new UpdateRevisionRequest(null, version("receipt_revision", copyId), null, null, new BigDecimal("330.75"), null, null, null, items));
        flush();

        assertThat(isCorrect(copyId)).isFalse();
    }

    @Test
    void updatingProductDictMatchesItemsToSimilarAlias() {
        UUID revisionId = revisionIdOf(createReceiptFromJson());
        createProduct("Cebula", "Cebula Zolta Luz");

        receiptFacade.updateProductDictInRevisionItems(user, revisionId);
        flush();

        assertThat(productNameOfItem(itemNamed(revisionId, ONION).id())).contains("Cebula");
        assertThat(productNameOfItem(itemNamed(revisionId, "OgórekSzkLuz").id())).isEmpty();
    }

    @Test
    void newReceiptItemsAreMatchedToExistingDictionary() {
        createProduct("Cebula", ONION);

        UUID revisionId = revisionIdOf(createReceiptFromJson());

        assertThat(productNameOfItem(itemNamed(revisionId, ONION).id())).contains("Cebula");
        assertThat(nameShownInRevision(revisionId, ONION)).isEqualTo("Cebula");
    }

    @Test
    void copyKeepsProductOfResolverRevisionItems() {
        createProduct("Cebula", ONION);
        UUID originalId = revisionIdOf(createReceiptFromJson());

        UUID copyId = copyRevision(originalId);

        assertThat(productNameOfItem(itemNamed(copyId, ONION).id())).contains("Cebula");
    }

    @Test
    void userCanSetAndClearProductOfItem() {
        UUID onionId = createProduct("Cebula", "Cebula");
        UUID copyId = copyRevision(revisionIdOf(createReceiptFromJson()));

        setProductOfItem(copyId, ONION, onionId);
        assertThat(productNameOfItem(itemNamed(copyId, ONION).id())).contains("Cebula");

        setProductOfItem(copyId, ONION, null);
        assertThat(productNameOfItem(itemNamed(copyId, ONION).id())).isEmpty();
    }

    @Test
    void newItemGetsChosenProductInsteadOfMatchedOne() {
        UUID onionId = createProduct("Cebula", "Cebula");
        createProduct("Inny", "Nowa pozycja");
        UUID copyId = copyRevision(revisionIdOf(createReceiptFromJson()));
        Set<ItemRequest> items = new HashSet<>(itemsOf(copyId));
        items.add(new ItemRequest(null, "Nowa pozycja", BigDecimal.ONE, BigDecimal.ONE, null, BigDecimal.ONE, 99, onionId));

        receiptFacade.updateRevision(user, copyId, revisionUpdate(copyId, items));
        flush();

        assertThat(productNameOfItem(itemNamed(copyId, "Nowa pozycja").id())).contains("Cebula");
    }

    @Test
    void settingUnknownProductIsRejected() {
        UUID copyId = copyRevision(revisionIdOf(createReceiptFromJson()));

        assertError(() -> setProductOfItem(copyId, ONION, UNKNOWN_ID), AppRuntimeExceptionMessages.PRODUCT_NOT_FOUND);
    }

    @Test
    void rewritingProductNamesRenamesOnlyUserRevisionItems() {
        UUID originalId = revisionIdOf(createReceiptFromJson());
        UUID copyId = copyRevision(originalId);
        createProduct("Cebula", ONION);
        receiptFacade.updateProductDictInRevisionItems(user, copyId);
        flush();

        receiptFacade.rewriteProductNameToItems(user, copyId);
        flush();

        assertThat(itemsOf(copyId)).extracting(ItemRequest::name).contains("Cebula").doesNotContain(ONION);
        assertThat(itemsOf(originalId)).extracting(ItemRequest::name).contains(ONION);
    }

    @Test
    void rewritingProductNamesIsRejectedForResolverRevision() {
        UUID originalId = revisionIdOf(createReceiptFromJson());

        assertError(() -> receiptFacade.rewriteProductNameToItems(user, originalId), AppRuntimeExceptionMessages.REVISION_NOT_USER_RESOLVER);
    }

    @Test
    void updatingAliasesIsRejectedForResolverRevision() {
        UUID originalId = revisionIdOf(createReceiptFromJson());

        assertError(() -> receiptFacade.learnProductsFromCorrections(user, originalId), AppRuntimeExceptionMessages.REVISION_NOT_USER_RESOLVER);
    }

    static Stream<Named<FacadeCall>> revisionCalls() {
        return Stream.of(
            Named.of("createRevisionCopy", (facade, user, id) -> facade.createRevisionCopy(user, id)),
            Named.of("getRevision", (facade, user, id) -> facade.getRevision(user, id)),
            Named.of("updateRevision", (facade, user, id) -> facade.updateRevision(user, id, new UpdateRevisionRequest(null, 0L, null, null, null, null, null, null, Set.of()))),
            Named.of("updateProductDictInRevisionItems", (facade, user, id) -> facade.updateProductDictInRevisionItems(user, id)),
            Named.of("deleteRevision", (facade, user, id) -> facade.deleteRevision(user, id)),
            Named.of("rewriteProductNameToItems", (facade, user, id) -> facade.rewriteProductNameToItems(user, id)));
    }

    @ParameterizedTest
    @MethodSource("revisionCalls")
    void unknownRevisionIsReported(FacadeCall call) {
        assertError(() -> call.call(receiptFacade, user, UNKNOWN_ID), AppRuntimeExceptionMessages.REVISION_NOT_FOUND);
    }

    @ParameterizedTest
    @MethodSource("revisionCalls")
    void otherUsersRevisionIsReportedAsUnknownAndStaysUntouched(FacadeCall call) {
        UUID receiptId = createReceiptFromJson();
        UUID copyId = copyRevision(revisionIdOf(receiptId));
        AuthenticatedUser stranger = new AuthenticatedUser(createUser(STRANGER_SUB));

        assertError(() -> call.call(receiptFacade, stranger, copyId), AppRuntimeExceptionMessages.REVISION_NOT_FOUND);
        flush();

        assertThat(count("SELECT count(*) FROM receipt_revision WHERE receipt_id = ?", receiptId)).isEqualTo(2);
        assertThat(itemsOf(copyId)).hasSize(36);
    }

    @Test
    void updatingAliasesOfOtherUsersRevisionIsRejectedAndChangesNothing() {
        UUID copyId = copyRevision(revisionIdOf(createReceiptFromJson()));
        renameItem(copyId, ONION, "Cebula");
        AuthenticatedUser stranger = new AuthenticatedUser(createUser(STRANGER_SUB));

        assertError(() -> receiptFacade.learnProductsFromCorrections(stranger, copyId), AppRuntimeExceptionMessages.REVISION_NOT_FOUND);
        flush();

        assertThat(count("SELECT count(*) FROM product")).isZero();
        assertThat(count("SELECT count(*) FROM receipt_item WHERE receipt_revision_id = ? AND product_id IS NOT NULL", copyId)).isZero();
    }

    @Test
    void revisionWithoutStrategyIsRejected() {
        UUID receiptId = createReceiptFromJson();
        UUID fileGroupId = jdbcTemplate.queryForObject("SELECT id FROM receipt_file_group WHERE receipt_id = ?", UUID.class, receiptId);

        assertError(() -> receiptFacade.createRevision(user, receiptId, new CreateRevisionRequest(fileGroupId, null)),
            AppRuntimeExceptionMessages.STRATEGY_NOT_FOUND);
    }

    @Test
    void updatingAliasesIsRejectedForUserRevisionWithoutParent() {
        UUID receiptId = createReceiptFromJson();
        UUID fileGroupId = jdbcTemplate.queryForObject("SELECT id FROM receipt_file_group WHERE receipt_id = ?", UUID.class, receiptId);
        UUID userRevisionId = receiptFacade.createRevision(user, receiptId,
            new CreateRevisionRequest(fileGroupId, ReceiptResolverStrategyType.USER)).revisionId();
        flush();

        assertError(() -> receiptFacade.learnProductsFromCorrections(user, userRevisionId),
            AppRuntimeExceptionMessages.REVISION_WITHOUT_PARENT);
    }

    @Test
    void revisionFromUnknownFileGroupIsRejected() {
        UUID receiptId = createReceiptFromJson();

        assertError(() -> receiptFacade.createRevision(user, receiptId, new CreateRevisionRequest(UNKNOWN_ID, ReceiptResolverStrategyType.BIEDRONKA_JSON)),
            AppRuntimeExceptionMessages.FILE_GROUP_NOT_FOUND);
    }

    @Test
    void receiptDetailsShowPreferredRevisionWithItemsAllRevisionsStoreAndFileParts() {
        UUID receiptId = createReceiptFromJson();
        UUID originalId = revisionIdOf(receiptId);
        UUID copyId = copyRevision(originalId);
        receiptFacade.updateRevision(user, copyId, preferredUpdate(copyId));
        List<byte[]> pages = List.of(png(3), png(1), png(2));
        UUID imagesGroupId = fileFacade.save(user, receiptId, pages);
        flush();

        GetReceiptDetailsResponse details = receiptFacade.getReceiptDetails(user, receiptId);

        assertThat(details.id()).isEqualTo(receiptId);
        assertThat(details.name()).isEqualTo("Biedronka 22.05");
        assertThat(details.description()).isEqualTo("test receipt");
        assertThat(details.walletId()).isEqualTo(walletId);
        assertThat(details.store()).extracting(StoreDetailsResponse::id, StoreDetailsResponse::brand, StoreDetailsResponse::city)
            .containsExactly(storeId, "Biedronka", "Poznań");
        assertThat(details.preferredRevision().id()).isEqualTo(copyId);
        assertThat(details.preferredRevision().resolver()).isEqualTo(ReceiptResolverStrategyType.USER);
        assertThat(details.preferredRevision().totalPrice()).isEqualByComparingTo("330.75");
        assertThat(details.preferredRevision().items()).hasSize(36)
            .extracting(GetReceiptDetailsResponse.ItemResponse::id)
            .containsExactlyInAnyOrderElementsOf(itemsOf(copyId).stream().map(ItemRequest::id).toList());
        assertThat(details.revisions())
            .extracting(GetReceiptDetailsResponse.RevisionResponse::id, GetReceiptDetailsResponse.RevisionResponse::isPreferredRevision)
            .containsExactlyInAnyOrder(tuple(originalId, false), tuple(copyId, true));
        assertThat(details.fileGroups()).extracting(GetReceiptDetailsResponse.FileGroupResponse::fileType)
            .containsExactlyInAnyOrder("JSON", "PNG");
        GetReceiptDetailsResponse.FileGroupResponse images = details.fileGroups().stream()
            .filter(group -> group.id().equals(imagesGroupId))
            .findFirst()
            .orElseThrow();
        assertThat(images.files()).extracting(GetReceiptDetailsResponse.FileGroupResponse.FileResponse::partNumber)
            .containsExactlyInAnyOrder(0, 1, 2);
    }

    @Test
    void receiptWithoutPreferredRevisionShowsNoPreferredItems() {
        UUID receiptId = createReceiptFromJson();

        GetReceiptDetailsResponse details = receiptFacade.getReceiptDetails(user, receiptId);

        assertThat(details.preferredRevision()).isNull();
        assertThat(details.revisions()).singleElement()
            .satisfies(revision -> assertThat(revision.resolver()).isEqualTo(ReceiptResolverStrategyType.BIEDRONKA_JSON));
    }

    @Test
    void revisionSummaryIsReplacedWithSentValues() {
        UUID copyId = copyRevision(revisionIdOf(createReceiptFromJson()));
        LocalDateTime paymentDate = LocalDateTime.of(2026, 6, 1, 18, 30);

        receiptFacade.updateRevision(user, copyId, summaryUpdate(copyId, new BigDecimal("100.00"), paymentDate, null, null));
        flush();
        receiptFacade.updateRevision(user, copyId, summaryUpdate(copyId, null, null, null, null));
        flush();

        GetRevisionResponse revision = receiptFacade.getRevision(user, copyId);
        assertThat(revision.totalPrice()).isNull();
        assertThat(revision.paymentDate()).isNull();
        assertThat(revision.isCorrect()).isFalse();
    }

    @Test
    void unmarkingPreferredRevisionLeavesReceiptWithoutPreferredRevision() {
        UUID receiptId = createReceiptFromJson();
        UUID originalId = revisionIdOf(receiptId);
        receiptFacade.updateRevision(user, originalId, preferredUpdate(originalId));
        flush();

        receiptFacade.updateRevision(user, originalId, summaryUpdate(originalId, null, null, false, null));
        flush();

        assertThat(isPreferred(originalId)).isFalse();
        assertThat(receiptFacade.getReceiptDetails(user, receiptId).preferredRevision()).isNull();
    }

    @Test
    void deletingPreferredRevisionLeavesReceiptWithoutPreferredRevision() {
        UUID receiptId = createReceiptFromJson();
        UUID copyId = copyRevision(revisionIdOf(receiptId));
        receiptFacade.updateRevision(user, copyId, preferredUpdate(copyId));
        flush();

        receiptFacade.deleteRevision(user, copyId);
        flush();

        assertThat(jdbcTemplate.queryForObject("SELECT preferred_revision_id FROM receipt WHERE id = ?", UUID.class, receiptId)).isNull();
        assertThat(receiptFacade.getReceiptDetails(user, receiptId).preferredRevision()).isNull();
    }

    @Test
    void updatingResolverRevisionChangesSummaryAndFlagsButKeepsItsItems() {
        UUID originalId = revisionIdOf(createReceiptFromJson());
        List<ItemRequest> itemsBefore = itemsOf(originalId);

        receiptFacade.updateRevision(user, originalId,
            new UpdateRevisionRequest(null, version("receipt_revision", originalId), null, null, new BigDecimal("1.00"), null, true, null, Set.of()));
        flush();

        assertThat(itemsOf(originalId)).containsExactlyElementsOf(itemsBefore);
        assertThat(isPreferred(originalId)).isTrue();
        assertThat(jdbcTemplate.queryForObject("SELECT total_price FROM receipt_revision WHERE id = ?", BigDecimal.class, originalId))
            .isEqualByComparingTo("1.00");
    }

    @Test
    void userCanMarkInconsistentRevisionAsCorrectButCannotUnmarkConsistentOne() {
        UUID originalId = revisionIdOf(createReceiptFromJson());
        UUID copyId = copyRevision(originalId);
        receiptFacade.updateRevision(user, copyId, summaryUpdate(copyId, new BigDecimal("1.00"), null, null, null));
        jdbcTemplate.update("UPDATE receipt_revision SET is_correct = false WHERE id = ?", copyId);
        flush();

        receiptFacade.updateRevision(user, copyId, summaryUpdate(copyId, null, null, null, true));
        receiptFacade.updateRevision(user, originalId, summaryUpdate(originalId, new BigDecimal("330.75"), null, null, false));
        flush();

        assertThat(isCorrect(copyId)).isTrue();
        assertThat(isCorrect(originalId)).isTrue();
    }

    @Test
    void revisionResolvedAgainFromStoredFileGroupGetsItemsAndDictionaryProducts() {
        UUID receiptId = createReceiptFromJson();
        UUID fileGroupId = jdbcTemplate.queryForObject("SELECT id FROM receipt_file_group WHERE receipt_id = ?", UUID.class, receiptId);
        UUID onionId = createProduct("Cebula", ONION);

        UUID revisionId = receiptFacade.createRevision(user, receiptId,
            new CreateRevisionRequest(fileGroupId, ReceiptResolverStrategyType.BIEDRONKA_JSON)).revisionId();
        flush();

        Map<String, Object> revision = jdbcTemplate.queryForMap(
            "SELECT resolver, source_file_group_id, is_correct, total_price FROM receipt_revision WHERE id = ?", revisionId);
        assertThat(revision.get("resolver")).isEqualTo("BIEDRONKA_JSON");
        assertThat(revision.get("source_file_group_id")).isEqualTo(fileGroupId);
        assertThat(isPreferred(revisionId)).isFalse();
        assertThat(revision.get("is_correct")).isEqualTo(true);
        assertThat((BigDecimal) revision.get("total_price")).isEqualByComparingTo("330.75");
        assertThat(itemsOf(revisionId)).hasSize(36);
        assertThat(productIdsOfItemsNamed(revisionId, ONION)).containsExactly(onionId);
        assertThat(receiptFacade.getReceiptRevisions(user, receiptId)).hasSize(2);
    }

    @Test
    void receiptLifecycleFromUploadThroughCorrectionAndLearningToDeletion() {
        UUID receiptId = createReceiptFromJson();
        UUID originalId = revisionIdOf(receiptId);
        UUID copyId = copyRevision(originalId);
        List<ItemRequest> copiedItems = itemsOf(copyId);
        ItemRequest removed = itemNamed(copyId, "OgórekSzkLuz");
        Set<ItemRequest> edited = copiedItems.stream()
            .filter(item -> !item.id().equals(removed.id()))
            .map(item -> item.name().equals(ONION)
                ? new ItemRequest(item.id(), "Cebula", item.amount(), item.unitPrice(), item.discount(), item.totalPrice(), item.position(), null)
                : item)
            .collect(Collectors.toCollection(HashSet::new));
        edited.add(new ItemRequest(null, "Torba", BigDecimal.ONE, new BigDecimal("0.99"), null, new BigDecimal("0.99"), 99, null));

        receiptFacade.updateRevision(user, copyId,
            new UpdateRevisionRequest(null, version("receipt_revision", copyId), null, null, new BigDecimal("330.75"), null, true, true, edited));
        flush();
        receiptFacade.learnProductsFromCorrections(user, copyId);
        flush();

        GetReceiptDetailsResponse details = receiptFacade.getReceiptDetails(user, receiptId);
        assertThat(details.preferredRevision().id()).isEqualTo(copyId);
        assertThat(details.preferredRevision().items()).hasSize(36)
            .extracting(GetReceiptDetailsResponse.ItemResponse::name)
            .contains("Cebula", "Torba", CHICKEN)
            .doesNotContain(ONION, removed.name());
        assertThat(details.preferredRevision().isCorrect()).isTrue();
        assertThat(productNameOfItem(itemNamed(originalId, ONION).id())).contains("Cebula");
        assertThat(productIdsOfItemsNamed(copyId, "Cebula")).isEqualTo(productIdsOfItemsNamed(originalId, ONION));
        assertThat(productIdsOfItemsNamed(copyId, CHICKEN)).hasSize(1).doesNotContainNull();
        assertThat(itemsOf(originalId)).hasSize(36).extracting(ItemRequest::name).contains(ONION, removed.name());
        assertError(() -> receiptFacade.deleteRevision(user, originalId),
            AppRuntimeExceptionMessages.CAN_NOT_DELETE_REVISION_WITH_CHILD_REVISIONS);

        receiptFacade.deleteReceipt(user, receiptId);
        flush();

        assertThat(count("SELECT count(*) FROM receipt WHERE id = ?", receiptId)).isZero();
        assertThat(count("SELECT count(*) FROM receipt_revision WHERE receipt_id = ?", receiptId)).isZero();
        assertThat(count("SELECT count(*) FROM receipt_file_group WHERE receipt_id = ?", receiptId)).isZero();
        assertThat(count("SELECT count(*) FROM product WHERE name = 'Cebula'")).isEqualTo(1);
    }

    private UpdateRevisionRequest summaryUpdate(UUID revisionId, BigDecimal totalPrice, LocalDateTime paymentDate,
                                                Boolean isPreferred, Boolean isCorrect) {
        return new UpdateRevisionRequest(null, version("receipt_revision", revisionId), null, null, totalPrice, paymentDate, isPreferred, isCorrect, Set.copyOf(itemsOf(revisionId)));
    }

    private static byte[] png(int size) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB), "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void renameItem(UUID revisionId, String from, String to) {
        Set<ItemRequest> items = itemsOf(revisionId).stream()
            .map(item -> item.name().equals(from)
                ? new ItemRequest(item.id(), to, item.amount(), item.unitPrice(), item.discount(), item.totalPrice(), item.position(), item.productId())
                : item)
            .collect(Collectors.toSet());
        receiptFacade.updateRevision(user, revisionId, revisionUpdate(revisionId, items));
        flush();
    }

    private void setProductOfItem(UUID revisionId, String itemName, UUID productId) {
        Set<ItemRequest> items = itemsOf(revisionId).stream()
            .map(item -> item.name().equals(itemName)
                ? new ItemRequest(item.id(), item.name(), item.amount(), item.unitPrice(), item.discount(), item.totalPrice(), item.position(), productId)
                : item)
            .collect(Collectors.toSet());
        receiptFacade.updateRevision(user, revisionId, revisionUpdate(revisionId, items));
        flush();
    }

    private UpdateRevisionRequest preferredUpdate(UUID revisionId) {
        GetRevisionResponse revision = receiptFacade.getRevision(user, revisionId);
        return new UpdateRevisionRequest(null, revision.version(), null, null, revision.totalPrice(), revision.paymentDate(), true, revision.isCorrect(), Set.copyOf(itemsOf(revisionId)));
    }

    private boolean isPreferred(UUID revisionId) {
        return jdbcTemplate.queryForObject("SELECT r.preferred_revision_id IS NOT DISTINCT FROM rr.id FROM receipt_revision rr JOIN receipt r ON r.id = rr.receipt_id WHERE rr.id = ?",
            Boolean.class, revisionId);
    }

    private boolean isCorrect(UUID revisionId) {
        return jdbcTemplate.queryForObject("SELECT is_correct FROM receipt_revision WHERE id = ?", Boolean.class, revisionId);
    }

    private Set<String> aliasesOf(String productName) {
        return Set.copyOf(jdbcTemplate.queryForList(
            "SELECT a.name FROM product_alias a JOIN product p ON p.id = a.product_id WHERE p.name = ?", String.class, productName));
    }

    private String nameShownInRevision(UUID revisionId, String itemName) {
        UUID itemId = itemNamed(revisionId, itemName).id();
        return receiptFacade.getRevision(user, revisionId).items().stream()
            .filter(item -> item.id().equals(itemId))
            .map(GetRevisionResponse.ItemResponse::name)
            .findFirst()
            .orElseThrow();
    }

    private UUID createReceiptFromJson() {
        UUID receiptId = receiptFacade.addReceipt(user, List.of(readResource(RECEIPT_JSON)),
            new CreateReceiptRequest("Biedronka 22.05", "test receipt", ReceiptResolverStrategyType.BIEDRONKA_JSON, walletId, storeId)).id();
        flush();
        return receiptId;
    }

    private UUID revisionIdOf(UUID receiptId) {
        return jdbcTemplate.queryForObject(
            "SELECT id FROM receipt_revision WHERE receipt_id = ? ORDER BY created_date LIMIT 1",
            UUID.class, receiptId);
    }

    private UUID copyRevision(UUID revisionId) {
        UUID copyId = receiptFacade.createRevisionCopy(user, revisionId).revisionId();
        flush();
        return copyId;
    }

    @Test
    void assignsDictionaryProductsToRevisionItemsAndKeepsItemsInRevision() {
        createProduct("Mleko", "Mleko 3,2% 1l");
        UUID revisionId = insertRevision(insertReceipt(walletId, storeId, "Receipt"), "USER", "10.00", true, null);
        UUID milkItemId = insertItem(revisionId, "Mleko 3,2% 1l", null);
        UUID unknownItemId = insertItem(revisionId, "Zzzz qqq", null);
        flush();

        receiptFacade.updateProductDictInRevisionItems(user, revisionId);
        flush();

        assertThat(itemsOf(revisionId)).extracting(ItemRequest::id).containsExactlyInAnyOrder(milkItemId, unknownItemId);
        assertThat(productNameOfItem(milkItemId)).contains("Mleko");
        assertThat(productNameOfItem(unknownItemId)).isEmpty();
    }

    @Test
    void doesNotAssignDictionaryProductsInSomeoneElsesRevision() {
        UUID revisionId = insertRevision(insertReceipt(walletId, storeId, "Receipt"), "USER", "10.00", true, null);
        AuthenticatedUser stranger = new AuthenticatedUser(createUser(STRANGER_SUB));
        flush();

        assertError(() -> receiptFacade.updateProductDictInRevisionItems(stranger, revisionId),
            AppRuntimeExceptionMessages.REVISION_NOT_FOUND);
    }

    private Set<UUID> productIdsOfItemsNamed(UUID revisionId, String name) {
        return itemsOf(revisionId).stream()
            .filter(item -> item.name().equals(name))
            .map(item -> jdbcTemplate.queryForObject("SELECT product_id FROM receipt_item WHERE id = ?", UUID.class, item.id()))
            .collect(Collectors.toSet());
    }

    private Set<String> aliasesOfProduct(UUID productId) {
        return Set.copyOf(jdbcTemplate.queryForList("SELECT name FROM product_alias WHERE product_id = ?", String.class, productId));
    }

    private List<ItemRequest> itemsOf(UUID revisionId) {
        return jdbcTemplate.query(
            "SELECT id, name, amount, unit_price, discount, total_price, position, product_id "
                + "FROM receipt_item WHERE receipt_revision_id = ? ORDER BY position, id",
            (rs, rowNum) -> new ItemRequest(rs.getObject("id", UUID.class), rs.getString("name"), rs.getBigDecimal("amount"),
                rs.getBigDecimal("unit_price"), rs.getBigDecimal("discount"), rs.getBigDecimal("total_price"), rs.getInt("position"),
                rs.getObject("product_id", UUID.class)),
            revisionId);
    }

    private ItemRequest itemNamed(UUID revisionId, String name) {
        return itemsOf(revisionId).stream()
            .filter(item -> item.name().equals(name))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No item named '" + name + "' in revision " + revisionId));
    }

    private Optional<String> productNameOfItem(UUID itemId) {
        return jdbcTemplate.queryForList(
            "SELECT p.name FROM receipt_item i JOIN product p ON p.id = i.product_id WHERE i.id = ?", String.class, itemId)
            .stream().findFirst();
    }

    private UUID createProduct(String name, String... aliases) {
        UUID productId = insertProduct(name);
        for (String alias : aliases) {
            insertAlias(productId, alias);
        }
        return productId;
    }

    private UpdateRevisionRequest revisionUpdate(UUID revisionId, Set<ItemRequest> items) {
        return new UpdateRevisionRequest(null, version("receipt_revision", revisionId), null, null, null, null, null, null, items);
    }
}
