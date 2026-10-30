package pl.madzierski.daniel.store;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import pl.madzierski.daniel.store.model.StoreBrandDto;

import java.util.Set;
import java.util.UUID;

public interface SqlStoreBrandQueryRepository extends StoreBrandQueryRepository, Repository<SqlStoreBrand, UUID> {

    @Query(value = """
        SELECT new pl.madzierski.daniel.store.model.StoreBrandDto(b.id, b.name) 
        FROM SqlStoreBrand b WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :query, '%'))
        """)
    Set<StoreBrandDto> findStoreBrandsByQuery(String query);
}
