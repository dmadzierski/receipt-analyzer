package pl.madzierski.daniel.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.modulith.events.EventExternalizationConfiguration;
import org.springframework.util.backoff.ExponentialBackOff;
import pl.madzierski.daniel.exception.AppRuntimeException;

import java.io.UncheckedIOException;

@Configuration
public class KafkaEventConfiguration {

    public static final String NAME = "receipt-analyzer.events";

    @Bean
    NewTopic eventsTopic() {
        return TopicBuilder.name(NAME).partitions(1).replicas(1).build();
    }

    @Bean
    DefaultErrorHandler eventListenerErrorHandler() {
        ExponentialBackOff backOff = new ExponentialBackOff(1_000, 2);
        backOff.setMaxInterval(60_000);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(backOff);
        errorHandler.addNotRetryableExceptions(UncheckedIOException.class, AppRuntimeException.class);
        return errorHandler;
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
            throw new UncheckedIOException(e);
        }
    }
}
