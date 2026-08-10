package pl.madzierski.daniel.receipt;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import pl.madzierski.daniel.receipt.model.ReceiptDto;

import java.util.Collection;
import java.util.Optional;

public interface SqlReceiptQueryRepository extends ReceiptQueryRepository, Repository<SqlReceipt, String> {

    @Query(value = "SELECT new pl.madzierski.daniel.receipt.model.ReceiptDto(r.id, r.createdDate, r.modifiedDate, r" +
        ".name, r.description, null) FROM SqlReceipt r WHERE r.wallet.id = " +
        ":walletId")
    Collection<ReceiptDto> getWalletReceipts(@Param("walletId") String walletId);

    @Query(value = "SELECT new pl.madzierski.daniel.receipt.model.ReceiptDto(r.id, r.createdDate, r.modifiedDate, r" +
        ".name, r.description, null) FROM SqlReceipt r WHERE r.id = :receiptId")
    Optional<ReceiptDto> findById(@Param("receiptId") String receiptId);
}
