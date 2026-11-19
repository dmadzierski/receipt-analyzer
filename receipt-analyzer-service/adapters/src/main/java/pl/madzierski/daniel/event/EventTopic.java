package pl.madzierski.daniel.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.modulith.events.EventExternalizationConfiguration;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;

@Configuration
public class EventTopic {

    public static final String NAME = "receipt-analyzer.events";

    @Bean
    NewTopic eventsTopic() {
        return TopicBuilder.name(NAME).partitions(1).replicas(1).build();
    }

    @Bean
    EventExternalizationConfiguration eventExternalizationConfiguration(ObjectMapper objectMapper) {
        return EventExternalizationConfiguration.externalizing()
            .select(EventExternalizationConfiguration.annotatedAsExternalized())
            .routeKey(PublishedEvent.class, event -> event.aggregateId().toString())
            .mapping(PublishedEvent.class, event -> toJson(objectMapper, event))
            .build();
    }

    private static String toJson(ObjectMapper objectMapper, PublishedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.EVENT_NOT_SERIALIZABLE, e);
        }
    }
}
