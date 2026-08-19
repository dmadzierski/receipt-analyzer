package pl.madzierski.daniel.store;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import pl.madzierski.daniel.store.model.StoreBrandDto;

import java.util.Set;

public interface SqlStoreBrandQueryRepository extends StoreBrandQueryRepository, Repository<SqlStoreBrand, String> {

    @Query(value = """
        SELECT new pl.madzierski.daniel.store.model.StoreBrandDto(b.id, b.name) 
        FROM SqlStoreBrand b WHERE b.name LIKE %:query%
        """)
    Set<StoreBrandDto> findStoreBrandsByQuery(String query);
}
