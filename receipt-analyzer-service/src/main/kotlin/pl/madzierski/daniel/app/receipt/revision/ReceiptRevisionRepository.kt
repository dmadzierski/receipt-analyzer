package pl.madzierski.daniel.app.receipt.revision

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import pl.madzierski.daniel.app.receipt.model.GetReceiptRevisionsResponse

@Repository
interface ReceiptRevisionRepository : JpaRepository<ReceiptRevisionEntity, String> {

    @Query(
        """
        SELECT r FROM ReceiptRevisionEntity r 
        LEFT JOIN FETCH r.items ri
        WHERE r.id = :id
        """
    )
    fun findReceiptRevisionEntitiesById(id: String): ReceiptRevisionEntity
    fun findReceiptRevisionEntityByReceiptId(receiptId: String): List<ReceiptRevisionEntity>
}