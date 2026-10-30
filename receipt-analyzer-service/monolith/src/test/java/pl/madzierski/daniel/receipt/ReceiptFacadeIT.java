package pl.madzierski.daniel.receipt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import pl.madzierski.daniel.IntegrationTest;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.receipt.model.CreateReceiptRequest;
import pl.madzierski.daniel.receipt.model.CreateRevisionRequest;
import pl.madzierski.daniel.receipt.model.GetRevisionResponse;
import pl.madzierski.daniel.receipt.model.UpdateReceiptRequest;
import pl.madzierski.daniel.receipt.model.UpdateRevisionRequest;
import pl.madzierski.daniel.receipt.model.UpdateRevisionRequest.ItemRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ReceiptFacadeIT extends IntegrationTest {

    private static final String USER_SUB = "test-user";
    private static final String RECEIPT_JSON = "receipts/biedronka/biedronka_e_receipt.json";
    private static final String ONION = "Cebula Żółta Luz";
    private static final String CHICKEN = "Fil  Z Piersi K kg";

    @Autowired
    private ReceiptFacade receiptFacade;

    private String walletId;
    private String storeId;

    @BeforeEach
    void createOwnerWalletAndStore() {
        String userId = createUser(USER_SUB);
        walletId = createWallet(userId, "Test wallet");
        storeId = createStore("Biedronka", "ul. Testowa 1", "Poznań");
    }

    @Test
    void createsReceiptWithRevisionFromBiedronkaJson() {
        String receiptId = createReceiptFromJson();

        Map<String, Object> revision = jdbcTemplate.queryForMap(
            "SELECT id, resolver, total_price, is_correct FROM receipt_revision WHERE receipt_id = ?", receiptId);
        assertThat(revision.get("resolver")).isEqualTo("BIEDRONKA_JSON");
        assertThat((BigDecimal) revision.get("total_price")).isEqualByComparingTo("330.75");
        assertThat(revision.get("is_correct")).isEqualTo(true);
        assertThat(itemsOf(revision.get("id").toString())).hasSize(36);
    }

    @Test
    void storesUploadedFileInUploadDirectory() throws IOException {
        String receiptId = createReceiptFromJson();

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
        String receiptId = createReceiptFromJson();
        String userRevisionId = copyRevision(revisionIdOf(receiptId));

        List<ItemRequest> items = itemsOf(userRevisionId);
        ItemRequest removed = items.getFirst();
        Set<ItemRequest> request = new HashSet<>(items.subList(1, items.size()));
        request.add(new ItemRequest(null, removed.name(), removed.amount(), removed.unitPrice(), removed.discount(),
            removed.totalPrice(), removed.position()));

        receiptFacade.updateRevision(userRevisionId, revisionUpdate(request));
        flush();

        Set<String> itemIds = itemsOf(userRevisionId).stream().map(ItemRequest::id).collect(Collectors.toSet());
        assertThat(itemIds).hasSize(36).doesNotContain(removed.id());
        assertThat(itemsOf(userRevisionId)).extracting(ItemRequest::name).contains(removed.name());
    }

    @Test
    void updatingReceiptKeepsItsWallet() {
        String receiptId = createReceiptFromJson();
        String otherStoreId = createStore("Lidl", "ul. Inna 2", "Kraków");

        receiptFacade.updateReceipt(receiptId, new UpdateReceiptRequest("  Weekly shopping ", "groceries", otherStoreId));
        flush();

        Map<String, Object> receipt = jdbcTemplate.queryForMap(
            "SELECT name, description, wallet_id, store_id FROM receipt WHERE id = ?", receiptId);
        assertThat(receipt.get("name")).isEqualTo("Weekly shopping");
        assertThat(receipt.get("description")).isEqualTo("groceries");
        assertThat(receipt.get("wallet_id")).isEqualTo(walletId);
        assertThat(receipt.get("store_id")).isEqualTo(otherStoreId);
    }

    @Test
    void updatingReceiptWithUnknownStoreFails() {
        String receiptId = createReceiptFromJson();

        assertError(() -> receiptFacade.updateReceipt(receiptId, new UpdateReceiptRequest("name", null, UNKNOWN_ID)),
            AppRuntimeExceptionMessages.STORE_NOT_FOUND);
    }

    static Stream<Named<Consumer<ReceiptFacade>>> callsWithUnknownReceipt() {
        return Stream.of(
            Named.of("getReceiptDetails", facade -> facade.getReceiptDetails(UNKNOWN_ID)),
            Named.of("createRevision", facade -> facade.createRevision(UNKNOWN_ID, new CreateRevisionRequest(UNKNOWN_ID, ReceiptResolverStrategyType.USER))),
            Named.of("updateReceipt", facade -> facade.updateReceipt(UNKNOWN_ID, new UpdateReceiptRequest("name", null, UNKNOWN_ID))),
            Named.of("deleteReceipt", facade -> facade.deleteReceipt(UNKNOWN_ID)));
    }

    @ParameterizedTest
    @MethodSource("callsWithUnknownReceipt")
    void unknownReceiptIsReported(Consumer<ReceiptFacade> call) {
        assertError(() -> call.accept(receiptFacade), AppRuntimeExceptionMessages.RECEIPT_NOT_FOUND);
    }

    @Test
    void deletingReceiptRemovesRevisionsItemsAndFileGroupsUsedByThem() {
        String receiptId = createReceiptFromJson();
        String originalId = revisionIdOf(receiptId);
        String copyId = copyRevision(originalId);
        String fileGroupId = jdbcTemplate.queryForObject("SELECT source_file_group_id FROM receipt_revision WHERE id = ?", String.class, copyId);
        assertThat(fileGroupId).isNotNull();

        receiptFacade.deleteReceipt(receiptId);
        flush();

        assertThat(count("SELECT count(*) FROM receipt WHERE id = ?", receiptId)).isZero();
        assertThat(count("SELECT count(*) FROM receipt_revision WHERE id IN (?, ?)", originalId, copyId)).isZero();
        assertThat(count("SELECT count(*) FROM receipt_item WHERE receipt_revision_id IN (?, ?)", originalId, copyId)).isZero();
        assertThat(count("SELECT count(*) FROM receipt_file_group WHERE id = ?", fileGroupId)).isZero();
        assertThat(count("SELECT count(*) FROM receipt_file WHERE file_group_id = ?", fileGroupId)).isZero();
    }

    @Test
    void copyIsEditableUserRevisionLinkedToOriginal() {
        String originalId = revisionIdOf(createReceiptFromJson());

        String copyId = copyRevision(originalId);

        Map<String, Object> copy = jdbcTemplate.queryForMap(
            "SELECT c.resolver, c.is_preferred_revision, c.is_correct, c.total_price, c.parent_receipt_revision_id, "
                + "c.source_file_group_id = o.source_file_group_id AS same_source "
                + "FROM receipt_revision c JOIN receipt_revision o ON o.id = ? WHERE c.id = ?", originalId, copyId);
        assertThat(copy.get("resolver")).isEqualTo("USER");
        assertThat(copy.get("is_preferred_revision")).isEqualTo(false);
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
        String receiptId = createReceiptFromJson();
        String originalId = revisionIdOf(receiptId);
        String copyId = copyRevision(originalId);

        assertError(() -> receiptFacade.deleteRevision(originalId), AppRuntimeExceptionMessages.CAN_NOT_DELETE_REVISION_WITH_CHILD_REVISIONS);

        receiptFacade.deleteRevision(copyId);
        flush();
        receiptFacade.deleteRevision(originalId);
        flush();

        assertThat(count("SELECT count(*) FROM receipt_revision WHERE receipt_id = ?", receiptId)).isZero();
        assertThat(count("SELECT count(*) FROM receipt_item WHERE receipt_revision_id IN (?, ?)", originalId, copyId)).isZero();
    }

    @Test
    void userCorrectionBecomesProductWithOriginalNameAsAlias() {
        String originalId = revisionIdOf(createReceiptFromJson());
        String copyId = copyRevision(originalId);
        renameItem(copyId, ONION, "Cebula");

        receiptFacade.updateAliasesByUserRevision(copyId);
        flush();

        assertThat(aliasesOf("Cebula")).containsExactlyInAnyOrder("Cebula", ONION);
        assertThat(productNameOfItem(itemNamed(originalId, ONION).id())).contains("Cebula");
        assertThat(nameShownInRevision(originalId, ONION)).isEqualTo("Cebula");
    }

    @Test
    void userCorrectionToExistingProductAddsAliasInsteadOfNewProduct() {
        String onionId = createProduct("Cebula", "Cebula");
        String originalId = revisionIdOf(createReceiptFromJson());
        String copyId = copyRevision(originalId);
        renameItem(copyId, ONION, "Cebula");

        receiptFacade.updateAliasesByUserRevision(copyId);
        flush();

        assertThat(count("SELECT count(*) FROM product WHERE name = 'Cebula'")).isEqualTo(1);
        assertThat(aliasesOf("Cebula")).containsExactlyInAnyOrder("Cebula", ONION);
        assertThat(jdbcTemplate.queryForObject("SELECT product_id FROM receipt_item WHERE id = ?", String.class,
            itemNamed(originalId, ONION).id())).isEqualTo(onionId);
    }

    @Test
    void dictionaryLearnedFromOneReceiptIsUsedForNextOne() {
        String firstCopyId = copyRevision(revisionIdOf(createReceiptFromJson()));
        renameItem(firstCopyId, ONION, "Cebula");
        receiptFacade.updateAliasesByUserRevision(firstCopyId);
        flush();

        String secondRevisionId = revisionIdOf(createReceiptFromJson());
        String secondCopyId = copyRevision(secondRevisionId);
        receiptFacade.updateAliasesByUserRevision(secondCopyId);
        flush();

        assertThat(count("SELECT count(*) FROM product")).isEqualTo(35);
        assertThat(count("SELECT count(*) FROM receipt_item WHERE receipt_revision_id = ? AND product_id IS NULL", secondRevisionId)).isZero();
        assertThat(nameShownInRevision(secondRevisionId, ONION)).isEqualTo("Cebula");
    }

    @Test
    void itemsWithSameNameShareOneProductAndRepeatedUpdateDoesNotDuplicateIt() {
        String originalId = revisionIdOf(createReceiptFromJson());
        String copyId = copyRevision(originalId);
        receiptFacade.updateAliasesByUserRevision(copyId);
        flush();

        receiptFacade.updateAliasesByUserRevision(copyId);
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
    void markingRevisionAsPreferredUnmarksOtherRevisionsOfReceipt() {
        String originalId = revisionIdOf(createReceiptFromJson());
        String copyId = copyRevision(originalId);
        receiptFacade.updateRevision(originalId, preferredUpdate());
        flush();

        receiptFacade.updateRevision(copyId, preferredUpdate());
        flush();

        assertThat(isPreferred(originalId)).isFalse();
        assertThat(isPreferred(copyId)).isTrue();
    }

    @Test
    void editWithConsistentPricesMarksRevisionAsCorrect() {
        String copyId = copyRevision(revisionIdOf(createReceiptFromJson()));
        jdbcTemplate.update("UPDATE receipt_revision SET is_correct = false WHERE id = ?", copyId);

        receiptFacade.updateRevision(copyId, revisionUpdate(Set.copyOf(itemsOf(copyId))));
        flush();

        assertThat(isCorrect(copyId)).isTrue();
    }

    @Test
    void editWithInconsistentPricesDoesNotMarkRevisionAsCorrect() {
        String copyId = copyRevision(revisionIdOf(createReceiptFromJson()));
        jdbcTemplate.update("UPDATE receipt_revision SET is_correct = false WHERE id = ?", copyId);
        Set<ItemRequest> items = itemsOf(copyId).stream()
            .map(item -> item.name().equals(ONION)
                ? new ItemRequest(item.id(), item.name(), item.amount(), item.unitPrice(), item.discount(), item.totalPrice().add(BigDecimal.ONE), item.position())
                : item)
            .collect(Collectors.toSet());

        receiptFacade.updateRevision(copyId, revisionUpdate(items));
        flush();

        assertThat(isCorrect(copyId)).isFalse();
    }

    @Test
    void updatingProductDictMatchesItemsToSimilarAlias() {
        String revisionId = revisionIdOf(createReceiptFromJson());
        createProduct("Cebula", "Cebula Zolta Luz");

        receiptFacade.updateProductDictInRevisionItems(revisionId);
        flush();

        assertThat(productNameOfItem(itemNamed(revisionId, ONION).id())).contains("Cebula");
        assertThat(productNameOfItem(itemNamed(revisionId, "OgórekSzkLuz").id())).isEmpty();
    }

    @Test
    void newReceiptItemsAreMatchedToExistingDictionary() {
        createProduct("Cebula", ONION);

        String revisionId = revisionIdOf(createReceiptFromJson());

        assertThat(productNameOfItem(itemNamed(revisionId, ONION).id())).contains("Cebula");
        assertThat(nameShownInRevision(revisionId, ONION)).isEqualTo("Cebula");
    }

    @Test
    void rewritingProductNamesRenamesOnlyUserRevisionItems() {
        String originalId = revisionIdOf(createReceiptFromJson());
        String copyId = copyRevision(originalId);
        createProduct("Cebula", ONION);
        receiptFacade.updateProductDictInRevisionItems(copyId);
        flush();

        receiptFacade.rewriteProductNameToItems(copyId);
        flush();

        assertThat(itemsOf(copyId)).extracting(ItemRequest::name).contains("Cebula").doesNotContain(ONION);
        assertThat(itemsOf(originalId)).extracting(ItemRequest::name).contains(ONION);
    }

    @Test
    void rewritingProductNamesIsRejectedForResolverRevision() {
        String originalId = revisionIdOf(createReceiptFromJson());

        assertError(() -> receiptFacade.rewriteProductNameToItems(originalId), AppRuntimeExceptionMessages.REVISION_NOT_USER_RESOLVER);
    }

    static Stream<Named<Consumer<ReceiptFacade>>> callsWithUnknownRevision() {
        return Stream.of(
            Named.of("createRevisionCopy", facade -> facade.createRevisionCopy(UNKNOWN_ID)),
            Named.of("getRevision", facade -> facade.getRevision(UNKNOWN_ID)),
            Named.of("updateRevision", facade -> facade.updateRevision(UNKNOWN_ID, revisionUpdate(Set.of()))),
            Named.of("updateProductDictInRevisionItems", facade -> facade.updateProductDictInRevisionItems(UNKNOWN_ID)),
            Named.of("deleteRevision", facade -> facade.deleteRevision(UNKNOWN_ID)),
            Named.of("rewriteProductNameToItems", facade -> facade.rewriteProductNameToItems(UNKNOWN_ID)));
    }

    @ParameterizedTest
    @MethodSource("callsWithUnknownRevision")
    void unknownRevisionIsReported(Consumer<ReceiptFacade> call) {
        assertError(() -> call.accept(receiptFacade), AppRuntimeExceptionMessages.REVISION_NOT_FOUND);
    }

    @Test
    void revisionWithoutStrategyIsRejected() {
        String receiptId = createReceiptFromJson();
        String fileGroupId = jdbcTemplate.queryForObject("SELECT id FROM receipt_file_group WHERE receipt_id = ?", String.class, receiptId);

        assertError(() -> receiptFacade.createRevision(receiptId, new CreateRevisionRequest(fileGroupId, null)),
            AppRuntimeExceptionMessages.STRATEGY_NOT_FOUND);
    }

    @Test
    void revisionFromUnknownFileGroupIsRejected() {
        String receiptId = createReceiptFromJson();

        assertError(() -> receiptFacade.createRevision(receiptId, new CreateRevisionRequest(UNKNOWN_ID, ReceiptResolverStrategyType.BIEDRONKA_JSON)),
            AppRuntimeExceptionMessages.FILE_GROUP_NOT_FOUND);
    }

    private void renameItem(String revisionId, String from, String to) {
        Set<ItemRequest> items = itemsOf(revisionId).stream()
            .map(item -> item.name().equals(from)
                ? new ItemRequest(item.id(), to, item.amount(), item.unitPrice(), item.discount(), item.totalPrice(), item.position())
                : item)
            .collect(Collectors.toSet());
        receiptFacade.updateRevision(revisionId, revisionUpdate(items));
        flush();
    }

    private static UpdateRevisionRequest preferredUpdate() {
        return new UpdateRevisionRequest(null, null, null, null, null, null, null, true, null, null);
    }

    private boolean isPreferred(String revisionId) {
        return jdbcTemplate.queryForObject("SELECT is_preferred_revision FROM receipt_revision WHERE id = ?", Boolean.class, revisionId);
    }

    private boolean isCorrect(String revisionId) {
        return jdbcTemplate.queryForObject("SELECT is_correct FROM receipt_revision WHERE id = ?", Boolean.class, revisionId);
    }

    private Set<String> aliasesOf(String productName) {
        return Set.copyOf(jdbcTemplate.queryForList(
            "SELECT a.name FROM product_alias a JOIN product p ON p.id = a.product_id WHERE p.name = ?", String.class, productName));
    }

    private String nameShownInRevision(String revisionId, String itemName) {
        String itemId = itemNamed(revisionId, itemName).id();
        return receiptFacade.getRevision(revisionId).items().stream()
            .filter(item -> item.id().equals(itemId))
            .map(GetRevisionResponse.ItemResponse::name)
            .findFirst()
            .orElseThrow();
    }

    private String createReceiptFromJson() {
        String receiptId = receiptFacade.addReceipt(USER_SUB, List.of(readResource(RECEIPT_JSON)),
            new CreateReceiptRequest("Biedronka 22.05", "test receipt", ReceiptResolverStrategyType.BIEDRONKA_JSON, walletId, storeId)).id();
        flush();
        return receiptId;
    }

    private String revisionIdOf(String receiptId) {
        return jdbcTemplate.queryForObject(
            "SELECT id FROM receipt_revision WHERE receipt_id = ? ORDER BY created_date LIMIT 1",
            String.class, receiptId);
    }

    private String copyRevision(String revisionId) {
        String copyId = receiptFacade.createRevisionCopy(revisionId).revisionId();
        flush();
        return copyId;
    }

    private List<ItemRequest> itemsOf(String revisionId) {
        return jdbcTemplate.query(
            "SELECT id, name, amount, unit_price, discount, total_price, position "
                + "FROM receipt_item WHERE receipt_revision_id = ? ORDER BY position, id",
            (rs, rowNum) -> new ItemRequest(rs.getString("id"), rs.getString("name"), rs.getBigDecimal("amount"),
                rs.getBigDecimal("unit_price"), rs.getBigDecimal("discount"), rs.getBigDecimal("total_price"), rs.getInt("position")),
            revisionId);
    }

    private ItemRequest itemNamed(String revisionId, String name) {
        return itemsOf(revisionId).stream()
            .filter(item -> item.name().equals(name))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No item named '" + name + "' in revision " + revisionId));
    }

    private Optional<String> productNameOfItem(String itemId) {
        return jdbcTemplate.queryForList(
            "SELECT p.name FROM receipt_item i JOIN product p ON p.id = i.product_id WHERE i.id = ?", String.class, itemId)
            .stream().findFirst();
    }

    private String createProduct(String name, String... aliases) {
        String productId = insertProduct(name);
        for (String alias : aliases) {
            insertAlias(productId, alias);
        }
        return productId;
    }

    private static UpdateRevisionRequest revisionUpdate(Set<ItemRequest> items) {
        return new UpdateRevisionRequest(null, null, null, null, null, null, null, null, null, items);
    }
}
