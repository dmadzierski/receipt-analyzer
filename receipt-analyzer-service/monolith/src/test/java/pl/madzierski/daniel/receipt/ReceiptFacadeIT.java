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
import pl.madzierski.daniel.user.model.AuthenticatedUser;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ReceiptFacadeIT extends IntegrationTest {

    private static final String USER_SUB = "test-user";
    private static final String RECEIPT_JSON = "receipts/biedronka/biedronka_e_receipt.json";
    private static final String ONION = "Cebula Żółta Luz";
    private static final String CHICKEN = "Fil  Z Piersi K kg";
    private static final String STRANGER_SUB = "stranger";

    @Autowired
    private ReceiptFacade receiptFacade;

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
            removed.totalPrice(), removed.position()));

        receiptFacade.updateRevision(user, userRevisionId, revisionUpdate(request));
        flush();

        Set<UUID> itemIds = itemsOf(userRevisionId).stream().map(ItemRequest::id).collect(Collectors.toSet());
        assertThat(itemIds).hasSize(36).doesNotContain(removed.id());
        assertThat(itemsOf(userRevisionId)).extracting(ItemRequest::name).contains(removed.name());
    }

    @Test
    void updatingReceiptKeepsItsWallet() {
        UUID receiptId = createReceiptFromJson();
        UUID otherStoreId = createStore("Lidl", "ul. Inna 2", "Kraków");

        receiptFacade.updateReceipt(user, receiptId, new UpdateReceiptRequest("  Weekly shopping ", "groceries", otherStoreId));
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
        UUID receiptId = createReceiptFromJson();

        assertError(() -> receiptFacade.updateReceipt(user, receiptId, new UpdateReceiptRequest("name", null, UNKNOWN_ID)),
            AppRuntimeExceptionMessages.STORE_NOT_FOUND);
    }

    interface FacadeCall {
        void call(ReceiptFacade facade, AuthenticatedUser user, UUID id);
    }

    static Stream<Named<FacadeCall>> receiptCalls() {
        return Stream.of(
            Named.of("getReceiptDetails", (facade, user, id) -> facade.getReceiptDetails(user, id)),
            Named.of("createRevision", (facade, user, id) -> facade.createRevision(user, id, new CreateRevisionRequest(UNKNOWN_ID, ReceiptResolverStrategyType.USER))),
            Named.of("updateReceipt", (facade, user, id) -> facade.updateReceipt(user, id, new UpdateReceiptRequest("name", null, UNKNOWN_ID))),
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

        receiptFacade.updateAliasesByUserRevision(user, copyId);
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

        receiptFacade.updateAliasesByUserRevision(user, copyId);
        flush();

        assertThat(count("SELECT count(*) FROM product WHERE name = 'Cebula'")).isEqualTo(1);
        assertThat(aliasesOf("Cebula")).containsExactlyInAnyOrder("Cebula", ONION);
        assertThat(jdbcTemplate.queryForObject("SELECT product_id FROM receipt_item WHERE id = ?", UUID.class,
            itemNamed(originalId, ONION).id())).isEqualTo(onionId);
    }

    @Test
    void dictionaryLearnedFromOneReceiptIsUsedForNextOne() {
        UUID firstCopyId = copyRevision(revisionIdOf(createReceiptFromJson()));
        renameItem(firstCopyId, ONION, "Cebula");
        receiptFacade.updateAliasesByUserRevision(user, firstCopyId);
        flush();

        UUID secondRevisionId = revisionIdOf(createReceiptFromJson());
        UUID secondCopyId = copyRevision(secondRevisionId);
        receiptFacade.updateAliasesByUserRevision(user, secondCopyId);
        flush();

        assertThat(count("SELECT count(*) FROM product")).isEqualTo(35);
        assertThat(count("SELECT count(*) FROM receipt_item WHERE receipt_revision_id = ? AND product_id IS NULL", secondRevisionId)).isZero();
        assertThat(nameShownInRevision(secondRevisionId, ONION)).isEqualTo("Cebula");
    }

    @Test
    void itemsWithSameNameShareOneProductAndRepeatedUpdateDoesNotDuplicateIt() {
        UUID originalId = revisionIdOf(createReceiptFromJson());
        UUID copyId = copyRevision(originalId);
        receiptFacade.updateAliasesByUserRevision(user, copyId);
        flush();

        receiptFacade.updateAliasesByUserRevision(user, copyId);
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
        UUID originalId = revisionIdOf(createReceiptFromJson());
        UUID copyId = copyRevision(originalId);
        receiptFacade.updateRevision(user, originalId, preferredUpdate());
        flush();

        receiptFacade.updateRevision(user, copyId, preferredUpdate());
        flush();

        assertThat(isPreferred(originalId)).isFalse();
        assertThat(isPreferred(copyId)).isTrue();
    }

    @Test
    void editWithConsistentPricesMarksRevisionAsCorrect() {
        UUID copyId = copyRevision(revisionIdOf(createReceiptFromJson()));
        jdbcTemplate.update("UPDATE receipt_revision SET is_correct = false WHERE id = ?", copyId);

        receiptFacade.updateRevision(user, copyId, revisionUpdate(Set.copyOf(itemsOf(copyId))));
        flush();

        assertThat(isCorrect(copyId)).isTrue();
    }

    @Test
    void editWithInconsistentPricesDoesNotMarkRevisionAsCorrect() {
        UUID copyId = copyRevision(revisionIdOf(createReceiptFromJson()));
        jdbcTemplate.update("UPDATE receipt_revision SET is_correct = false WHERE id = ?", copyId);
        Set<ItemRequest> items = itemsOf(copyId).stream()
            .map(item -> item.name().equals(ONION)
                ? new ItemRequest(item.id(), item.name(), item.amount(), item.unitPrice(), item.discount(), item.totalPrice().add(BigDecimal.ONE), item.position())
                : item)
            .collect(Collectors.toSet());

        receiptFacade.updateRevision(user, copyId, revisionUpdate(items));
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

    static Stream<Named<FacadeCall>> revisionCalls() {
        return Stream.of(
            Named.of("createRevisionCopy", (facade, user, id) -> facade.createRevisionCopy(user, id)),
            Named.of("getRevision", (facade, user, id) -> facade.getRevision(user, id)),
            Named.of("updateRevision", (facade, user, id) -> facade.updateRevision(user, id, revisionUpdate(Set.of()))),
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
    void updatingAliasesOfOtherUsersRevisionChangesNothing() {
        UUID copyId = copyRevision(revisionIdOf(createReceiptFromJson()));
        renameItem(copyId, ONION, "Cebula");
        AuthenticatedUser stranger = new AuthenticatedUser(createUser(STRANGER_SUB));

        receiptFacade.updateAliasesByUserRevision(stranger, copyId);
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
    void revisionFromUnknownFileGroupIsRejected() {
        UUID receiptId = createReceiptFromJson();

        assertError(() -> receiptFacade.createRevision(user, receiptId, new CreateRevisionRequest(UNKNOWN_ID, ReceiptResolverStrategyType.BIEDRONKA_JSON)),
            AppRuntimeExceptionMessages.FILE_GROUP_NOT_FOUND);
    }

    private void renameItem(UUID revisionId, String from, String to) {
        Set<ItemRequest> items = itemsOf(revisionId).stream()
            .map(item -> item.name().equals(from)
                ? new ItemRequest(item.id(), to, item.amount(), item.unitPrice(), item.discount(), item.totalPrice(), item.position())
                : item)
            .collect(Collectors.toSet());
        receiptFacade.updateRevision(user, revisionId, revisionUpdate(items));
        flush();
    }

    private static UpdateRevisionRequest preferredUpdate() {
        return new UpdateRevisionRequest(null, null, null, null, null, null, null, true, null, null);
    }

    private boolean isPreferred(UUID revisionId) {
        return jdbcTemplate.queryForObject("SELECT is_preferred_revision FROM receipt_revision WHERE id = ?", Boolean.class, revisionId);
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

    private List<ItemRequest> itemsOf(UUID revisionId) {
        return jdbcTemplate.query(
            "SELECT id, name, amount, unit_price, discount, total_price, position "
                + "FROM receipt_item WHERE receipt_revision_id = ? ORDER BY position, id",
            (rs, rowNum) -> new ItemRequest(rs.getObject("id", UUID.class), rs.getString("name"), rs.getBigDecimal("amount"),
                rs.getBigDecimal("unit_price"), rs.getBigDecimal("discount"), rs.getBigDecimal("total_price"), rs.getInt("position")),
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

    private static UpdateRevisionRequest revisionUpdate(Set<ItemRequest> items) {
        return new UpdateRevisionRequest(null, null, null, null, null, null, null, null, null, items);
    }
}
