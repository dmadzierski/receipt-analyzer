package pl.madzierski.daniel.app.receipt.revision.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

interface ReceiptItemRepository extends JpaRepository<ReceiptItemEntity, String> {


    @Query(value = """
            SELECT r FROM ReceiptItemEntity r
                LEFT JOIN FETCH r.receiptRevision rr
                LEFT JOIN FETCH r.parentItem rp
                LEFT JOIN FETCH rp.receiptRevision rrp
            WHERE 
                rr.resolver = 'USER' AND 
                rrp.resolver != 'USER' AND 
                rr.id = :revisionId AND
                r.nameDict IS NULL AND
                rp.nameDict IS NULL 
            """
    )
    List<ReceiptItemEntity> findAllMissingAliasesInRevision(String revisionId);
}
