package pl.madzierski.daniel.app.receipt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceiptRepository extends JpaRepository<ReceiptEntity, String> {
    @Query(value = "SELECT * FROM receipt WHERE user_sub = :userSub ORDER BY created_date DESC", nativeQuery = true)
    List<ReceiptEntity> getReceiptList(@Param("userSub") String userSub);

    ReceiptEntity findReceiptEntityById(String id);
}
