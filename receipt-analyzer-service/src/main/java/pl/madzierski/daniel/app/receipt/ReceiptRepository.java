package pl.madzierski.daniel.app.receipt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.app.wallet.WalletEntity;

import java.util.List;

@Repository
public interface ReceiptRepository extends JpaRepository<ReceiptEntity, String> {
    @Query(value = "SELECT * FROM receipt INNER JOIN wallet ON wallet.id = receipt.wallet_id WHERE user_sub = :userSub AND wallet.id = :walletId ORDER BY created_date DESC", nativeQuery = true)
    List<ReceiptEntity> getReceiptList(@Param("userSub") String userSub, String walletId);

    ReceiptEntity findReceiptEntityById(String id);

    List<ReceiptEntity> wallet(WalletEntity wallet);
}
