package pl.madzierski.daniel.app.receipt

import org.springframework.stereotype.Service

@Service
class ReceiptProvider(
    val receiptRepository: ReceiptRepository,
) {
    fun findById(receiptId: String) = receiptRepository.findById(receiptId)

}