package pl.madzierski.daniel.readmodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.UUID;

@Document("processed_events")
record ProcessedEvent(@Id UUID id, @Indexed UUID correlationId, @Indexed(expireAfter = "7d") Date processedAt) {
}

interface ProcessedEventRepository extends MongoRepository<ProcessedEvent, UUID> {
    long countByCorrelationId(UUID correlationId);
}
