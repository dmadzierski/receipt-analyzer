package pl.madzierski.daniel.app.wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<WalletEntity, String> {
    List<WalletEntity> findAllByUserSub(String currentUserSub);

    @Query(
            value = """
                        SELECT w, r
                        FROM WalletEntity w
                        LEFT JOIN FETCH w.receipts r
                        WHERE w.id = :walletId AND w.userSub = :userSub
                    """
    )
    Optional<WalletEntity> findWalletEntitiesByIdAndUserSub(String walletId, String userSub);
}
