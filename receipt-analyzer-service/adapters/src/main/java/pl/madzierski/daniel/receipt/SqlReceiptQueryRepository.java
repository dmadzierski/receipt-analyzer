package pl.madzierski.daniel.receipt;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import pl.madzierski.daniel.receipt.model.ReceiptDto;
import pl.madzierski.daniel.receipt.projection.WalletReceiptProjection;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface SqlReceiptQueryRepository extends ReceiptQueryRepository, Repository<SqlReceipt, UUID> {

    @Query("""
        SELECT new pl.madzierski.daniel.receipt.projection.WalletReceiptProjection(r.id, r.name, rr.paymentDate, rr.isCorrect, rr.totalPrice, r.createdDate, s.id)
        FROM SqlReceipt r
        LEFT JOIN r.store s
        LEFT JOIN r.receiptRevisions rr ON rr.isCorrect = true
        WHERE r.wallet.id = :walletId
        """)
    Collection<WalletReceiptProjection> getWalletReceipts(@Param("walletId") UUID walletId);

    @Query(value = "SELECT new pl.madzierski.daniel.receipt.model.ReceiptDto(r.id, r.createdDate, r.modifiedDate, r" +
        ".name, r.description, new pl.madzierski.daniel.wallet.model.WalletQuery(r.wallet.id), new pl.madzierski.daniel.store.model.StoreQuery(r.store.id)) FROM SqlReceipt r WHERE r.id = :receiptId")
    Optional<ReceiptDto> findById(@Param("receiptId") UUID receiptId);
}
