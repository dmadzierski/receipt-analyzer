package pl.madzierski.daniel.store.model;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record StoreBrandListResponse (
    Set<StoreBrandItem> storeBrands
){

    public StoreBrandListResponse(Collection<StoreBrandDto> storeBrands) {
        this(storeBrands.stream().map(StoreBrandItem::new).collect(Collectors.toSet()));
    }

    public record StoreBrandItem(UUID id, String name) {
        StoreBrandItem(StoreBrandDto storeBrand) {
            this(storeBrand.id(), storeBrand.name());
        }
    }
}
