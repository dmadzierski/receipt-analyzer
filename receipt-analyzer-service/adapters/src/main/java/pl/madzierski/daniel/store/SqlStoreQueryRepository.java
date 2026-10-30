package pl.madzierski.daniel.store;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import pl.madzierski.daniel.store.model.StoreDto;

import java.util.Collection;
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
        FROM SqlStore s LEFT JOIN s.brand b WHERE s.id IN :storeIds
        """)
    Set<StoreDto> findStoresByIds(Collection<String> storeIds);

    @Query(value = """
        SELECT new pl.madzierski.daniel.store.model.StoreDto(s.id, b.name, s.address, s.city, s.postalCode, s.country)
        FROM SqlStore s LEFT JOIN s.brand b
        WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.address) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(s.city) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.postalCode) LIKE LOWER(CONCAT('%', :query, '%'))
        """)
    Set<StoreDto> findStoresByQuery(String query);
}
