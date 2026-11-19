package pl.madzierski.daniel.receipt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import pl.madzierski.daniel.IntegrationTest;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.product_dict.ProductDictFacade;
import pl.madzierski.daniel.product_dict.model.AliasToLearn;
import pl.madzierski.daniel.product_dict.model.ProductDictQuery;
import pl.madzierski.daniel.receipt.event.RevisionChanged;
import pl.madzierski.daniel.receipt.event.RevisionDeleted;
import pl.madzierski.daniel.receipt.model.CreateReceiptRequest;
import pl.madzierski.daniel.receipt.model.GetReceiptDetailsResponse;
import pl.madzierski.daniel.receipt.model.GetRevisionResponse;
import pl.madzierski.daniel.receipt.model.UpdateRevisionRequest;
import pl.madzierski.daniel.receipt.model.UpdateRevisionRequest.ItemRequest;
import pl.madzierski.daniel.user.model.AuthenticatedUser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ReadModelProjectionIT extends IntegrationTest {

    private static final String RECEIPT_JSON = "receipts/biedronka/biedronka_e_receipt.json";
    private static final String ONION = "Cebula Żółta Luz";

    @Autowired
    private ReceiptFacade receiptFacade;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ProductDictFacade productDictFacade;

    private AuthenticatedUser user;
    private UUID walletId;
    private UUID storeId;

    @BeforeEach
    void createOwnerWalletAndStore() {
        UUID userId = createUser("projection-user");
        user = new AuthenticatedUser(userId);
        walletId = createWallet(userId, "Wallet");
        storeId = createStore("Biedronka", "ul. Testowa 1", "Poznań");
        entityManager.flush();
        readModelRebuild.projectDirectly();
    }

    @Test
    void eventsProjectedOneByOneGiveTheSameReadModelAsRebuildFromDatabase() {
        UUID receiptId = addReceipt();
        UUID originalId = revisionIdOf(receiptId);
        UUID copyId = receiptFacade.createRevisionCopy(user, originalId).revisionId();
        clearSession();
        receiptFacade.updateRevision(user, copyId, update(copyId, renamed(copyId, ONION, "Cebula"), true));
        clearSession();
        receiptFacade.learnProductsFromCorrections(user, copyId);
        clearSession();
        UUID otherProductId = productDictFacade.learnAliases(List.of(new AliasToLearn("Cebula Czerwona Luz", "Cebula czerwona", null)))
            .values().iterator().next().id();
        clearSession();
        receiptFacade.reassignProductDict(new ProductDictQuery(otherProductId), List.of(productIdOfItem(originalId, ONION)));
        clearSession();
        receiptFacade.createRevisionCopy(user, copyId);

        projectOutboxEvents();
        ReadModel projected = readModel(receiptId, originalId, copyId);
        readModelRebuild.projectDirectly();
        ReadModel rebuilt = readModel(receiptId, originalId, copyId);

        assertThat(projected).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(rebuilt);
        assertThat(projected.details().preferredRevision().id()).isEqualTo(copyId);
        assertThat(projected.original().items()).filteredOn(item -> otherProductId.equals(item.productId()))
            .extracting(GetRevisionResponse.ItemResponse::name)
            .containsOnly("Cebula czerwona");
    }

    @Test
    void eventDeliveredTwiceIsAppliedOnce() {
        UUID eventId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        UUID receiptId = UUID.randomUUID();

        readModelProjector.project(eventId, UUID.randomUUID(), revision(revisionId, receiptId, 1, "10.00"));
        readModelProjector.project(UUID.randomUUID(), UUID.randomUUID(), revision(revisionId, receiptId, 2, "20.00"));
        readModelProjector.project(eventId, UUID.randomUUID(), revision(revisionId, receiptId, 1, "10.00"));

        assertThat(mongoTemplate.findById(revisionId, MongoRevision.class).totalPrice()).isEqualByComparingTo("20.00");
    }

    @Test
    void olderSnapshotDeliveredLateDoesNotOverwriteNewerOne() {
        UUID revisionId = UUID.randomUUID();
        UUID receiptId = UUID.randomUUID();

        readModelProjector.project(UUID.randomUUID(), UUID.randomUUID(), revision(revisionId, receiptId, 2, "20.00"));
        readModelProjector.project(UUID.randomUUID(), UUID.randomUUID(), revision(revisionId, receiptId, 1, "10.00"));

        assertThat(mongoTemplate.findById(revisionId, MongoRevision.class).totalPrice()).isEqualByComparingTo("20.00");
    }

    @Test
    void snapshotDeliveredAfterDeletionDoesNotBringRevisionBack() {
        UUID revisionId = UUID.randomUUID();
        UUID receiptId = UUID.randomUUID();

        readModelProjector.project(UUID.randomUUID(), UUID.randomUUID(), revision(revisionId, receiptId, 1, "10.00"));
        readModelProjector.project(UUID.randomUUID(), UUID.randomUUID(), new RevisionDeleted(revisionId));
        readModelProjector.project(UUID.randomUUID(), UUID.randomUUID(), revision(revisionId, receiptId, 2, "20.00"));

        assertThat(mongoTemplate.findById(revisionId, MongoRevision.class)).isNull();
    }

    @Test
    void deletedReceiptDisappearsWithItsRevisionsAndFileGroups() {
        UUID receiptId = addReceipt();
        receiptFacade.deleteReceipt(user, receiptId);

        projectOutboxEvents();

        assertError(() -> receiptFacade.getReceiptDetails(user, receiptId), AppRuntimeExceptionMessages.RECEIPT_NOT_FOUND);
        assertThat(receiptFacade.getReceiptRevisions(user, receiptId)).isEmpty();
        assertThat(readModelProjector.processedCount(outboxEvents().getLast().correlationId())).isEqualTo(outboxEvents().size());
    }

    private void clearSession() {
        publishPendingEvents();
        entityManager.clear();
    }

    private ReadModel readModel(UUID receiptId, UUID originalId, UUID copyId) {
        return new ReadModel(receiptFacade.getReceiptDetails(user, receiptId), receiptFacade.getRevision(user, originalId),
            receiptFacade.getRevision(user, copyId));
    }

    private RevisionChanged revision(UUID revisionId, UUID receiptId, long version, String total) {
        return new RevisionChanged(revisionId, version, receiptId, ReceiptResolverStrategyType.USER, LocalDateTime.now(), new BigDecimal(total),
            null, false, null, null, List.of());
    }

    private UUID addReceipt() {
        return receiptFacade.addReceipt(user, List.of(readResource(RECEIPT_JSON)),
            new CreateReceiptRequest("Biedronka", "test", ReceiptResolverStrategyType.BIEDRONKA_JSON, walletId, storeId)).id();
    }

    private UUID revisionIdOf(UUID receiptId) {
        clearSession();
        return jdbcTemplate.queryForObject("SELECT id FROM receipt_revision WHERE receipt_id = ?", UUID.class, receiptId);
    }

    private UUID productIdOfItem(UUID revisionId, String name) {
        entityManager.flush();
        return jdbcTemplate.queryForObject("SELECT product_id FROM receipt_item WHERE receipt_revision_id = ? AND name = ?",
            UUID.class, revisionId, name);
    }

    private Set<ItemRequest> renamed(UUID revisionId, String from, String to) {
        entityManager.flush();
        return jdbcTemplate.query(
                "SELECT id, name, amount, unit_price, discount, total_price, position, product_id FROM receipt_item WHERE receipt_revision_id = ?",
                (rs, rowNum) -> new ItemRequest(rs.getObject("id", UUID.class), rs.getString("name"), rs.getBigDecimal("amount"),
                    rs.getBigDecimal("unit_price"), rs.getBigDecimal("discount"), rs.getBigDecimal("total_price"), rs.getInt("position"),
                    rs.getObject("product_id", UUID.class)),
                revisionId).stream()
            .map(item -> item.name().equals(from)
                ? new ItemRequest(item.id(), to, item.amount(), item.unitPrice(), item.discount(), item.totalPrice(), item.position(), item.productId())
                : item)
            .collect(Collectors.toSet());
    }

    private UpdateRevisionRequest update(UUID revisionId, Set<ItemRequest> items, boolean preferred) {
        return new UpdateRevisionRequest(null, version("receipt_revision", revisionId), null, null, new BigDecimal("330.75"), null, preferred, null, items);
    }

    private record ReadModel(GetReceiptDetailsResponse details, GetRevisionResponse original, GetRevisionResponse copy) {
    }
}
