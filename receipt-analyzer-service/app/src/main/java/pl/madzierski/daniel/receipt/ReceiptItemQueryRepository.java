package pl.madzierski.daniel.receipt;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import pl.madzierski.daniel.receipt.model.ReceiptItemDto;

import java.util.Collection;
import java.util.List;

public interface ReceiptItemQueryRepository extends Repository<ReceiptItemEntity, String> {

    @Query(nativeQuery = true, value = """
            SELECT ri.id AS id, ri.product_dict_id as productDictId, IF(rr.resolver = 1, ri.name, COALESCE(pd.name, ri.name)) AS name, ri.amount AS amount, ri.unit_price AS unitPrice, ri.discount as discount, ri.total_price AS totalPrice, ri.position AS position
            FROM receipt_item ri
            LEFT JOIN receipt_revision rr ON ri.receipt_revision_id = rr.id
            LEFT JOIN product_dict pd ON pd.id = ri.product_dict_id
            WHERE ri.receipt_revision_id = :revisionId
        """)
    Collection<ReceiptItemDto> findReceiptItemsByRevisionId(String revisionId);


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
    List<ReceiptItemDto> findAllMissingAliasesInRevision(String revisionId);
}
