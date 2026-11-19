package pl.madzierski.daniel.readmodel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import pl.madzierski.daniel.event.EventPublisher;
import pl.madzierski.daniel.event.IntegrationEvent;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReadModelRebuild {

    private final List<ReadModelSnapshotSource> sources;
    private final ReadModelProjector projector;
    private final EventPublisher eventPublisher;
    private final TransactionTemplate transactionTemplate;
    private final ProjectionMetaRepository projectionMetaRepository;

    @Value("${read-model.rebuild-on-start:true}")
    private boolean rebuildOnStart;

    @Value("${read-model.force-rebuild:false}")
    private boolean forceRebuild;

    @EventListener(ApplicationReadyEvent.class)
    void rebuildOnStart() {
        if (forceRebuild) rebuild();
        else if (rebuildOnStart) rebuildIfNeverBuilt();
    }

    public boolean rebuildIfNeverBuilt() {
        if (projectionMetaRepository.existsById(ProjectionMeta.READ_MODEL)) return false;
        rebuild();
        return true;
    }

    private void rebuild() {
        log.info("Publishing snapshots of all aggregates to rebuild the read model");
        republish();
        projectionMetaRepository.save(new ProjectionMeta(ProjectionMeta.READ_MODEL, new Date()));
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
}
