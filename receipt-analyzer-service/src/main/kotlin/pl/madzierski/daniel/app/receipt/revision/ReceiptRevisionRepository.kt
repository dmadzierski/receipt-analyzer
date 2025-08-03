package pl.madzierski.daniel.app.receipt.revision

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReceiptRevisionRepository : JpaRepository<ReceiptRevisionEntity, String> {
}