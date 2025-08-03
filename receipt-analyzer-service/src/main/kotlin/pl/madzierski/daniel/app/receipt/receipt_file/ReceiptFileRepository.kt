package pl.madzierski.daniel.app.receipt.receipt_file

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReceiptFileRepository : JpaRepository<ReceiptFileEntity, String>