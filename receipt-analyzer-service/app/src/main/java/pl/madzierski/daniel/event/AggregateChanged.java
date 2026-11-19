package pl.madzierski.daniel.event;

public interface AggregateChanged extends IntegrationEvent {
    long version();
}
