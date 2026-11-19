package pl.madzierski.daniel.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import pl.madzierski.daniel.IntegrationTest;
import pl.madzierski.daniel.wallet.event.WalletChanged;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class EventExternalizationIT extends IntegrationTest {

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void removePublications() {
        jdbcTemplate.update("DELETE FROM event_publication");
    }

    @Test
    void committedEventIsSentToKafkaAsEnvelopeAndRemovedFromRegistry() {
        UUID walletId = UUID.randomUUID();
        new TransactionTemplate(transactionManager).executeWithoutResult(status ->
            eventPublisher.publish(new WalletChanged(walletId, 1, UUID.randomUUID(), "Home", LocalDateTime.now())));

        PublishedEvent published = envelopeOf(recordsOf(walletId, 1).getFirst());

        assertThat(published.type()).isEqualTo("WalletChanged");
        assertThat(published.aggregateId()).isEqualTo(walletId);
        assertThat(published.payload()).contains("\"name\":\"Home\"");
        assertThat(published.id()).isNotNull();
        assertThat(published.correlationId()).isNotNull();
        await().atMost(Duration.ofSeconds(10)).until(() -> count("SELECT count(*) FROM event_publication") == 0);
    }

    @Test
    void eventOfRolledBackTransactionIsNeverRegistered() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            eventPublisher.publish(new WalletChanged(UUID.randomUUID(), 1, UUID.randomUUID(), "Home", LocalDateTime.now()));
            status.setRollbackOnly();
        });

        assertThat(count("SELECT count(*) FROM event_publication")).isZero();
    }


    private List<ConsumerRecord<String, String>> recordsOf(UUID aggregateId, int expected) {
        Properties properties = new Properties();
        properties.put("auto.offset.reset", "earliest");
        try (Consumer<String, String> consumer = consumerFactory.createConsumer("externalization-test-" + UUID.randomUUID(), null, null, properties)) {
            consumer.subscribe(List.of(EventTopic.NAME));
            List<ConsumerRecord<String, String>> found = new ArrayList<>();
            long deadline = System.currentTimeMillis() + 15_000;
            while (found.size() < expected && System.currentTimeMillis() < deadline) {
                consumer.poll(Duration.ofMillis(500)).forEach(record -> {
                    if (aggregateId.toString().equals(record.key())) found.add(record);
                });
            }
            return found;
        }
    }

    private PublishedEvent envelopeOf(ConsumerRecord<String, String> record) {
        try {
            return objectMapper.readValue(record.value(), PublishedEvent.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
