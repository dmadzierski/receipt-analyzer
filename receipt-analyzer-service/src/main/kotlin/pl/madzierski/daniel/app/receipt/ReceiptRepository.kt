package pl.madzierski.daniel.app.receipt

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import pl.madzierski.daniel.app.receipt.model.GetReceiptListItemResponse

@Repository
interface ReceiptRepository : JpaRepository<ReceiptEntity, String> {

    @Query(
        "SELECT id, name, description, created_date FROM receipt WHERE user_sub = :userSub ORDER BY created_date DESC",
        nativeQuery = true
    )
    fun getReceiptList(@Param("userSub") userSub: String): List<GetReceiptListItemResponse>

    fun findReceiptEntityById(id: String): ReceiptEntity

}