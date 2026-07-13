package pl.madzierski.daniel.receipt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ReceiptRevisionRepository extends JpaRepository<ReceiptRevisionEntity, String> {
}
