package pl.madzierski.daniel.receipt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.madzierski.daniel.IntegrationTest;
import pl.madzierski.daniel.digitization.ReceiptResolverStrategyType;
import pl.madzierski.daniel.product_dict.model.ProductDictQuery;
import pl.madzierski.daniel.receipt.model.CreateReceiptRequest;
import pl.madzierski.daniel.receipt.model.UpdateRevisionRequest;
import pl.madzierski.daniel.user.model.AuthenticatedUser;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class ReceiptEventsIT extends IntegrationTest {

    private static final String RECEIPT_JSON = "receipts/biedronka/biedronka_e_receipt.json";

    @Autowired
    private ReceiptFacade receiptFacade;

    private AuthenticatedUser user;
    private UUID walletId;
    private UUID storeId;

    @BeforeEach
    void createOwnerWalletAndStore() {
        UUID userId = createUser("events-user");
        user = new AuthenticatedUser(userId);
        walletId = createWallet(userId, "Wallet");
        storeId = createStore("Biedronka", "ul. Testowa 1", "Poznań");
    }

    @Test
    void addingReceiptPublishesReceiptFileGroupAndRevisionInOneCorrelation() {
        UUID receiptId = addReceipt();

        List<OutboxEvent> events = outboxEvents();
        assertThat(events).extracting(OutboxEvent::type)
            .containsExactly("ReceiptChanged", "FileGroupChanged", "RevisionChanged");
        assertThat(events).extracting(OutboxEvent::correlationId).containsOnly(events.getFirst().correlationId());
        assertThat(events.getFirst().aggregateId()).isEqualTo(receiptId);
        assertThat(events.getFirst().payload()).contains("\"walletId\":\"" + walletId, "\"storeId\":\"" + storeId);
        assertThat(events.get(1).payload()).contains("\"partNumber\":0", "\"path\":\"");
        assertThat(events.getLast().payload()).contains("\"receiptId\":\"" + receiptId, "\"resolver\":\"BIEDRONKA_JSON\"");
    }

    @Test
    void choosingPreferredRevisionPublishesReceiptWithPreferredRevision() {
        UUID receiptId = addReceipt();
        UUID revisionId = revisionIdOf(receiptId);
        int before = outboxEvents().size();

        receiptFacade.updateRevision(user, revisionId,
            new UpdateRevisionRequest(null, version("receipt_revision", revisionId), null, null, null, null, true, null, Set.of()));

        List<OutboxEvent> events = outboxEvents().subList(before, outboxEvents().size());
        assertThat(events).extracting(OutboxEvent::type, OutboxEvent::aggregateId)
            .containsExactly(tuple("ReceiptChanged", receiptId), tuple("RevisionChanged", revisionId));
        assertThat(events.getFirst().payload()).contains("\"preferredRevisionId\":\"" + revisionId);
    }

    @Test
    void revisionSnapshotCarriesVersionOfSavedRevision() {
        UUID revisionId = revisionIdOf(addReceipt());
        int before = outboxEvents().size();

        receiptFacade.updateRevision(user, revisionId,
            new UpdateRevisionRequest(null, version("receipt_revision", revisionId), null, null, null, null, null, null, Set.of()));

        List<OutboxEvent> events = outboxEvents().subList(before, outboxEvents().size());
        assertThat(events).extracting(OutboxEvent::type).containsExactly("RevisionChanged");
        assertThat(events.getFirst().payload()).contains("\"version\":" + version("receipt_revision", revisionId) + ",");
    }

    @Test
    void deletingPreferredRevisionUnmarksItOnReceiptBeforeRevisionIsDeleted() {
        UUID receiptId = addReceipt();
        UUID copyId = receiptFacade.createRevisionCopy(user, revisionIdOf(receiptId)).revisionId();
        receiptFacade.updateRevision(user, copyId, new UpdateRevisionRequest(null, version("receipt_revision", copyId), null, null, null, null, true, null, Set.of()));
        int before = outboxEvents().size();

        receiptFacade.deleteRevision(user, copyId);

        List<OutboxEvent> events = outboxEvents().subList(before, outboxEvents().size());
        assertThat(events).extracting(OutboxEvent::type, OutboxEvent::aggregateId)
            .containsExactly(tuple("ReceiptChanged", receiptId), tuple("RevisionDeleted", copyId));
        assertThat(events.getFirst().payload()).contains("\"preferredRevisionId\":null");
    }

    @Test
    void deletingReceiptPublishesOnlyReceiptDeletion() {
        UUID receiptId = addReceipt();
        int before = outboxEvents().size();

        receiptFacade.deleteReceipt(user, receiptId);

        assertThat(outboxEvents().subList(before, outboxEvents().size()))
            .extracting(OutboxEvent::type, OutboxEvent::aggregateId)
            .containsExactly(tuple("ReceiptDeleted", receiptId));
    }

    @Test
    void reassigningProductsPublishesReassignmentToPrimaryProduct() {
        UUID primaryId = insertProduct("Cebula");
        UUID mergedId = insertProduct("Cebula czerwona");

        receiptFacade.reassignProductDict(new ProductDictQuery(primaryId), List.of(mergedId));

        assertThat(outboxEvents()).singleElement()
            .satisfies(event -> {
                assertThat(event.type()).isEqualTo("ItemProductsReassigned");
                assertThat(event.aggregateId()).isEqualTo(primaryId);
                assertThat(event.payload()).contains(mergedId.toString());
            });
    }

    private UUID addReceipt() {
        UUID receiptId = receiptFacade.addReceipt(user, List.of(readResource(RECEIPT_JSON)),
            new CreateReceiptRequest("Biedronka", null, ReceiptResolverStrategyType.BIEDRONKA_JSON, walletId, storeId)).id();
        flush();
        return receiptId;
    }

    private UUID revisionIdOf(UUID receiptId) {
        return jdbcTemplate.queryForObject("SELECT id FROM receipt_revision WHERE receipt_id = ?", UUID.class, receiptId);
    }
}
