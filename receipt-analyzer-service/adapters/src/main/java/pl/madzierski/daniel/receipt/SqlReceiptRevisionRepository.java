package pl.madzierski.daniel.receipt;


import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

interface SqlReceiptRevisionRepository extends JpaRepository<SqlReceiptRevision, String> {
    SqlReceiptRevision save(SqlReceiptRevision revision);

    @Query("""
        SELECT r FROM SqlReceiptRevision r LEFT JOIN FETCH r.items item LEFT JOIN FETCH item.parentItem \
        parentItem\
         WHERE r.id = \
        :revisionId""")
    Optional<SqlReceiptRevision> findByIdWithItems(String revisionId);
}

@AllArgsConstructor
@Repository
class ReceiptRevisionRepositoryImpl implements ReceiptRevisionRepository {
    private final SqlReceiptRevisionRepository repository;

    @Override
    public ReceiptRevision save(ReceiptRevision revision) {
        return repository.save(SqlReceiptRevision.fromReceiptRevision(revision))
            .toReceiptRevision();
    }

    @Override
    public Optional<ReceiptRevision> findByIdWithItems(String revisionId) {
        return repository.findByIdWithItems(revisionId)
            .map(SqlReceiptRevision::toReceiptRevision);
    }

    @Override
    public Optional<ReceiptRevision> findById(String revisionId) {
        return repository.findById(revisionId)
            .map(SqlReceiptRevision::toReceiptRevision);
    }
}
