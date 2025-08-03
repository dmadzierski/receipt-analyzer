package pl.madzierski.daniel.app.receipt

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReceiptRepository : JpaRepository<ReceiptEntity, String> {

}