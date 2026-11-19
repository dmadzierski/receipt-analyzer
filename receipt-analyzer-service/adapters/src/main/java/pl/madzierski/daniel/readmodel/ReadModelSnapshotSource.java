package pl.madzierski.daniel.readmodel;

import pl.madzierski.daniel.event.IntegrationEvent;

import java.util.stream.Stream;

public interface ReadModelSnapshotSource {
    int order();

    Stream<IntegrationEvent> snapshots();
}
