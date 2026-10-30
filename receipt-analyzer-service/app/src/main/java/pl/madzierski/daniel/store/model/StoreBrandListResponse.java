package pl.madzierski.daniel.store.model;

import java.util.Set;
import java.util.UUID;

public record StoreBrandListResponse (
    Set<StoreBrandItem> storeBrands
){

    public static StoreBrandItem storeBrandMapper(StoreBrandDto storeBrandDto) {
        return new StoreBrandItem(storeBrandDto.id(), storeBrandDto.name());
    }

    public record StoreBrandItem(UUID id, String name) {
    }
}
