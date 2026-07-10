package pl.madzierski.daniel.app.receipt.revision.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.app.product_dict.ProductDictEntity;

import java.util.List;

@Repository
interface ReceiptItemRepository extends JpaRepository<ReceiptItemEntity, String> {


    @Query(value = """
            SELECT r FROM ReceiptItemEntity r
                LEFT JOIN FETCH r.receiptRevision rr
                LEFT JOIN FETCH r.parentItem rp
                LEFT JOIN FETCH rp.receiptRevision rrp
            WHERE 
                rr.resolver = ReceiptResolverStrategyType.USER AND 
                rrp.resolver != ReceiptResolverStrategyType.USER AND 
                rr.id = :revisionId AND
                r.nameDict IS NULL AND
                rp.nameDict IS NULL 
            """
    )
    List<ReceiptItemEntity> findAllMissingAliasesInRevision(String revisionId);

    @Modifying
    @Query("""
            UPDATE ReceiptItemEntity r
            SET r.nameDict = :primaryDict
            WHERE r.nameDict.id IN (:productDictIdList)
            """)
    int reassignProductDict(ProductDictEntity primaryDict, List<String> productDictIdList);
}
