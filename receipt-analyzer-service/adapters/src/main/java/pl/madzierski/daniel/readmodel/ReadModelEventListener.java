package pl.madzierski.daniel.readmodel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pl.madzierski.daniel.event.KafkaEventConfiguration;
import pl.madzierski.daniel.event.EventTypes;
import pl.madzierski.daniel.event.PublishedEvent;

import java.io.UncheckedIOException;

@Component
@RequiredArgsConstructor
class ReadModelEventListener {

    static final String LISTENER_ID = "read-model";

    private final ReadModelProjector projector;
    private final ObjectMapper objectMapper;

    @KafkaListener(id = LISTENER_ID, idIsGroup = false, topics = KafkaEventConfiguration.NAME, autoStartup = "${read-model.listener.enabled:true}")
    void on(ConsumerRecord<String, String> record) {
        PublishedEvent published = read(record.value(), PublishedEvent.class);
        projector.project(published.id(), published.correlationId(),
            read(published.payload(), EventTypes.classOf(published.type())));
    }

    private <T> T read(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
