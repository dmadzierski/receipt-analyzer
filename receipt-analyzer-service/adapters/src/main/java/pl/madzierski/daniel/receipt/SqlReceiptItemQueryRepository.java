package pl.madzierski.daniel.receipt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.madzierski.daniel.receipt.model.ReceiptItemDto;

import java.util.List;
import java.util.UUID;

public interface SqlReceiptItemQueryRepository extends ReceiptItemQueryRepository, JpaRepository<SqlReceiptItem, UUID> {

    @Query(nativeQuery = true, value = """
            SELECT ri.id AS id, ri.product_id AS "productDictId", CASE WHEN rr.resolver = 'USER' THEN ri.name ELSE COALESCE(pd.name, ri.name) END AS name, ri.amount AS amount, ri.unit_price AS "unitPrice", ri.discount AS discount, ri.total_price AS "totalPrice", ri.position AS position
            FROM receipt_item ri
            LEFT JOIN receipt_revision rr ON ri.receipt_revision_id = rr.id
            LEFT JOIN product pd ON pd.id = ri.product_id
            WHERE ri.receipt_revision_id = :revisionId
        """)
    List<ReceiptItemDto> findReceiptItemsByRevisionId(UUID revisionId);
}
