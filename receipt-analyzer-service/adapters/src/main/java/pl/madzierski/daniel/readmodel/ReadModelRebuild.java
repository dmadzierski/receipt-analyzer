package pl.madzierski.daniel.readmodel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import pl.madzierski.daniel.event.EventPublisher;
import pl.madzierski.daniel.event.IntegrationEvent;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReadModelRebuild {

    private static final String META_ID = "read-model";

    private final List<ReadModelSnapshotSource> sources;
    private final ReadModelProjector projector;
    private final EventPublisher eventPublisher;
    private final TransactionTemplate transactionTemplate;
    private final MongoTemplate mongoTemplate;

    @Value("${read-model.rebuild-on-start:true}")
    private boolean rebuildOnStart;

    @Order(1)
    @EventListener(ApplicationReadyEvent.class)
    void rebuildWhenNeverBuilt() {
        if (!rebuildOnStart || mongoTemplate.exists(query(where("_id").is(META_ID)), ProjectionMeta.class)) return;
        log.info("Read model was never built, publishing snapshots of all aggregates");
        republish();
        mongoTemplate.save(new ProjectionMeta(META_ID, new Date()));
    }

    public void republish() {
        transactionTemplate.executeWithoutResult(status -> {
            projector.clear();
            snapshots().forEach(eventPublisher::publish);
        });
    }

    public void projectDirectly() {
        projector.clear();
        snapshots().forEach(projector::project);
    }

    private Stream<IntegrationEvent> snapshots() {
        return sources.stream()
            .sorted(Comparator.comparingInt(ReadModelSnapshotSource::order))
            .flatMap(ReadModelSnapshotSource::snapshots);
    }

    @Document("projection_meta")
    record ProjectionMeta(@Id String id, Date rebuiltAt) {
    }
}
