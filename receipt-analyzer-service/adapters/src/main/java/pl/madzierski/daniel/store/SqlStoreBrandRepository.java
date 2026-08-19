package pl.madzierski.daniel.store;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.store.model.StoreBrand;

import java.util.Optional;

interface SqlStoreBrandRepository extends JpaRepository<SqlStoreBrand, String> {
}

@AllArgsConstructor
@Repository
class StoreBrandRepositoryImpl implements StoreBrandRepository {

    private final SqlStoreBrandRepository repository;

    @Override
    public StoreBrand save(StoreBrand storeBrand) {
        return this.repository.save(SqlStoreBrand.fromStoreBrand(storeBrand)).toStoreBrand();
    }

    @Override
    public Optional<StoreBrand> findById(String id) {
        return this.repository.findById(id).map(SqlStoreBrand::toStoreBrand);
    }
}
