package pl.madzierski.daniel.readmodel;

import pl.madzierski.daniel.event.IntegrationEvent;

public interface Projector {
    void apply(IntegrationEvent event);

    void clear();
}
