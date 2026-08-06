package pl.madzierski.daniel.receipt;


import lombok.AllArgsConstructor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

interface SqlReceiptRevisionRepository extends CrudRepository<SqlReceiptRevision, String> {
    SqlReceiptRevision save(SqlReceiptRevision revision);

    Optional<SqlReceiptRevision> findById(String revisionId);
}

@AllArgsConstructor
@Repository
class ReceiptRevisionRepositoryImpl implements ReceiptRevisionRepository {
    private final SqlReceiptRevisionRepository repository;

    @Override
    public ReceiptRevision save(ReceiptRevision revision) {
        return repository.save(SqlReceiptRevision.fromReceiptRevision(revision)).toReceiptRevision();
    }

    @Override
    public Optional<ReceiptRevision> findById(String revisionId) {
        return repository.findById(revisionId).map(SqlReceiptRevision::toReceiptRevision);
    }
}
