package pl.madzierski.daniel.store.model;

import java.util.UUID;

public record StoreBrandDto(
    UUID id,
    String name
) {
}
