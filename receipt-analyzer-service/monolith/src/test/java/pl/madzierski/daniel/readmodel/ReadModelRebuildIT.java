package pl.madzierski.daniel.readmodel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import pl.madzierski.daniel.IntegrationTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReadModelRebuildIT extends IntegrationTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ProjectionMetaRepository projectionMetaRepository;

    @AfterEach
    void forgetRebuild() {
        projectionMetaRepository.deleteAll();
    }

    @Test
    void republishedSnapshotsBuildReadModelOfDataThatHasNeverBeenProjected() {
        UUID userId = createUser("rebuild-user");
        UUID walletId = createWallet(userId, "Home");
        UUID storeId = createStore("Lidl", "ul. Testowa 2", "Gniezno");
        UUID receiptId = insertReceipt(walletId, storeId, "Zakupy");
        insertRevision(receiptId, "USER", "12.50", true, true, LocalDateTime.of(2026, 5, 22, 10, 4));
        entityManager.flush();

        readModelRebuild.republish();
        projectOutboxEvents();

        assertThat(outboxEvents()).extracting(OutboxEvent::type)
            .contains("WalletChanged", "StoreBrandChanged", "StoreChanged", "ReceiptChanged", "RevisionChanged");
        assertThat(mongoTemplate.getCollection("wallets").countDocuments()).isEqualTo(1);
        assertThat(mongoTemplate.getCollection("stores").find().first()).containsEntry("brand", "Lidl");
        assertThat(mongoTemplate.getCollection("receipts").find().first()).containsEntry("name", "Zakupy");
        assertThat(mongoTemplate.getCollection("revisions").countDocuments()).isEqualTo(1);
    }

    @Test
    void readModelIsRebuiltOnlyWhenItWasNeverBuilt() {
        createWallet(createUser("rebuild-once-user"), "Home");
        entityManager.flush();

        assertThat(readModelRebuild.rebuildIfNeverBuilt()).isTrue();
        int published = outboxEvents().size();
        assertThat(readModelRebuild.rebuildIfNeverBuilt()).isFalse();

        assertThat(published).isEqualTo(1);
        assertThat(outboxEvents()).hasSize(published);
    }
}
