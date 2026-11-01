package pl.madzierski.daniel.receipt;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import pl.madzierski.daniel.receipt.model.ReceiptRevisionDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SqlReceiptRevisionQueryRepository extends ReceiptRevisionQueryRepository,
    Repository<SqlReceiptRevision, UUID> {

    @Query("""
        SELECT new pl.madzierski.daniel.receipt.model.ReceiptRevisionDto(
            rev.id,
            rev.createdDate,
            rev.name,
            rev.resolver,
            rev.totalPrice,
            rev.paymentDate,
            rev.isPreferredRevision,
            rev.isCorrect,
            null,
            rev.receipt.id,
            rev.revision
        )
        FROM SqlReceiptRevision rev
        WHERE rev.receipt.id = :receiptId AND rev.receipt.ownerId = :ownerId
        """
    )
    List<ReceiptRevisionDto> getRevisionsByReceiptId(UUID receiptId, UUID ownerId);

    @Query("""
        SELECT new pl.madzierski.daniel.receipt.model.ReceiptRevisionDto(
            rev.id,
            rev.createdDate,
            rev.name,
            rev.resolver,
            rev.totalPrice,
            rev.paymentDate,
            rev.isPreferredRevision,
            rev.isCorrect,
            null,
            rev.receipt.id,
            rev.revision
        )
        FROM SqlReceiptRevision rev
        WHERE rev.id = :revisionId AND rev.receipt.ownerId = :ownerId
        """
    )
    Optional<ReceiptRevisionDto> getRevisionById(UUID revisionId, UUID ownerId);

}
