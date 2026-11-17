package pl.madzierski.daniel.receipt;


import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.product_dict.SqlProductDictQuery;
import pl.madzierski.daniel.product_dict.model.ProductDictQuery;
import pl.madzierski.daniel.user.model.UserQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SqlReceiptRevisionRepository extends JpaRepository<SqlReceiptRevision, UUID> {
    @Query("SELECT r FROM SqlReceiptRevision r LEFT JOIN FETCH r.items item LEFT JOIN FETCH item.parentItem parentItem " +
        "WHERE r.id = :revisionId AND r.receipt.ownerId = :ownerId")
    Optional<SqlReceiptRevision> findAccessibleWithItems(UUID revisionId, UUID ownerId);

    @Query("SELECT r FROM SqlReceiptRevision r WHERE r.id = :revisionId AND r.receipt.ownerId = :ownerId")
    Optional<SqlReceiptRevision> findAccessible(UUID revisionId, UUID ownerId);

    boolean existsByParentReceiptRevisionId(UUID parentRevisionId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM SqlReceiptRevision r WHERE r.receipt.id = :receiptId")
    void deleteByReceiptId(UUID receiptId);

    @Modifying
    @Query("""
        UPDATE SqlReceiptItem i
        SET i.product = :dict
        WHERE i.product.id IN (:productDictIdList)
        """)
    void reassignProductDict(SqlProductDictQuery dict, List<UUID> productDictIdList);
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
    public Optional<ReceiptRevision> findByIdWithItems(UUID revisionId, UserQuery user) {
        return repository.findAccessibleWithItems(revisionId, user.id())
            .map(SqlReceiptRevision::toReceiptRevision);
    }

    @Override
    public Optional<ReceiptRevision> findAccessible(UUID revisionId, UserQuery user) {
        return repository.findAccessible(revisionId, user.id())
            .map(SqlReceiptRevision::toReceiptRevision);
    }

    @Override
    public boolean hasChildRevisions(UUID revisionId) {
        return repository.existsByParentReceiptRevisionId(revisionId);
    }

    @Override
    public void deleteById(UUID revisionId) {
        repository.deleteById(revisionId);
    }

    @Override
    public void deleteAllByReceiptId(UUID receiptId) {
        repository.deleteByReceiptId(receiptId);
    }

    @Override
    public void reassignProductDict(ProductDictQuery dict, List<UUID> productDictIdList) {
        repository.reassignProductDict(SqlProductDictQuery.fromProductDict(dict), productDictIdList);
    }
}
