package pl.madzierski.daniel.readmodel;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.madzierski.daniel.event.AggregateChanged;
import pl.madzierski.daniel.event.AggregateDeleted;
import pl.madzierski.daniel.event.IntegrationEvent;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReadModelProjector {

    private final List<Projection> projections;
    private final ProcessedEventRepository processedEventRepository;
    private final ProjectedVersionRepository projectedVersionRepository;

    public void project(UUID eventId, UUID correlationId, IntegrationEvent event) {
        if (processedEventRepository.existsById(eventId)) return;
        project(event);
        processedEventRepository.save(new ProcessedEvent(eventId, correlationId, new Date()));
    }

    public void project(IntegrationEvent event) {
        switch (event) {
            case AggregateChanged changed -> {
                if (!isNewerThanProjected(changed)) return;
                apply(event);
                projectedVersionRepository.save(new ProjectedVersion(changed.id(), changed.version(), false));
            }
            case AggregateDeleted deleted -> {
                apply(event);
                projectedVersionRepository.save(new ProjectedVersion(deleted.id(), 0, true));
            }
            default -> apply(event);
        }
    }

    public long processedCount(UUID correlationId) {
        return processedEventRepository.countByCorrelationId(correlationId);
    }

    public void clear() {
        projections.forEach(Projection::clear);
        projectedVersionRepository.deleteAll();
    }

    private boolean isNewerThanProjected(AggregateChanged event) {
        return projectedVersionRepository.findById(event.id())
            .map(projected -> projected.isOlderThan(event.version()))
            .orElse(true);
    }

    private void apply(IntegrationEvent event) {
        projections.forEach(projection -> projection.apply(event));
    }
}
