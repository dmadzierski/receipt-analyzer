package pl.madzierski.daniel.store;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import pl.madzierski.daniel.store.model.StoreDto;

import java.util.Optional;
import java.util.Set;

public interface SqlStoreQueryRepository extends StoreQueryRepository, Repository<SqlStore, String> {

    @Query(value = """
        SELECT new pl.madzierski.daniel.store.model.StoreDto(s.id, b.name, s.address, s.city, s.postalCode, s.country)
        FROM SqlStore s LEFT JOIN s.brand b WHERE s.id = :storeId
        """)
    Optional<StoreDto> findStoreById(String storeId);

    @Query(value = """
        SELECT new pl.madzierski.daniel.store.model.StoreDto(s.id, b.name, s.address, s.city, s.postalCode, s.country)
        FROM SqlStore s LEFT JOIN s.brand b WHERE b.name LIKE %:query% OR s.address LIKE %:query% OR s.city
        LIKE %:query% OR s.postalCode LIKE %:query%
        """)
    Set<StoreDto> findStoresByQuery(String query);
}
