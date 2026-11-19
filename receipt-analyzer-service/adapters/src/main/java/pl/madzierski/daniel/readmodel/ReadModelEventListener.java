package pl.madzierski.daniel.readmodel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pl.madzierski.daniel.event.EventTopic;
import pl.madzierski.daniel.event.EventTypes;
import pl.madzierski.daniel.event.PublishedEvent;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;

@Component
@RequiredArgsConstructor
public class ReadModelEventListener {

    public static final String LISTENER_ID = "read-model";

    private final ReadModelProjector projector;
    private final ObjectMapper objectMapper;

    @KafkaListener(id = LISTENER_ID, idIsGroup = false, topics = EventTopic.NAME, autoStartup = "${read-model.listener.enabled:true}")
    void on(ConsumerRecord<String, String> record) {
        PublishedEvent published = read(record.value(), PublishedEvent.class);
        projector.project(published.id(), published.correlationId(),
            read(published.payload(), EventTypes.classOf(published.type())));
    }

    private <T> T read(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.EVENT_NOT_DESERIALIZABLE, e);
        }
    }
}
