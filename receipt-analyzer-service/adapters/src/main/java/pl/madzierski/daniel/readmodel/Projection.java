package pl.madzierski.daniel.readmodel;

import pl.madzierski.daniel.event.IntegrationEvent;

public interface Projection {
    void apply(IntegrationEvent event);

    void clear();
}
