package pl.madzierski.daniel.receipt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.madzierski.daniel.receipt.model.ReceiptItemDto;

import java.util.Collection;
import java.util.List;

public interface SqlReceiptItemQueryRepository extends ReceiptItemQueryRepository, JpaRepository<SqlReceiptItem, String> {

    @Query(nativeQuery = true, value = """
            SELECT ri.id AS id, ri.product_id as productDictId, IF(rr.resolver = 1, ri.name, COALESCE(pd.name, ri.name)) AS name, ri.amount AS amount, ri.unit_price AS unitPrice, ri.discount as discount, ri.total_price AS totalPrice, ri.position AS position
            FROM receipt_item ri
            LEFT JOIN receipt_revision rr ON ri.receipt_revision_id = rr.id
            LEFT JOIN product pd ON pd.id = ri.product_id
            WHERE ri.receipt_revision_id = :revisionId
        """)
    Collection<ReceiptItemDto> findReceiptItemsByRevisionId(String revisionId);


    @Query(value = """
        SELECT new pl.madzierski.daniel.receipt.model.ReceiptItemDto(
            r.id,
            NULL,
            r.name,
            r.amount,
            r.unitPrice,
            r.discount,
            r.totalPrice,
            r.position,
            new pl.madzierski.daniel.receipt.model.ReceiptItemDto(
                rp.id,
                NULL,
                rp.name,
                rp.amount,
                rp.unitPrice,
                rp.discount,
                rp.totalPrice,
                rp.position
            )
        )
        FROM SqlReceiptItem r
            LEFT JOIN r.receiptRevision rr
            LEFT JOIN r.parentItem rp
            LEFT JOIN rp.receiptRevision rrp
        WHERE
            rr.resolver = pl.madzierski.daniel.receipt.ReceiptResolverStrategyType.USER AND
            rrp.resolver <> pl.madzierski.daniel.receipt.ReceiptResolverStrategyType.USER AND
            rr.id = :revisionId AND
            r.product IS NULL AND
            rp.product IS NULL
        """
    )
    List<ReceiptItemDto> findAllMissingAliasesInRevision(String revisionId);
}
