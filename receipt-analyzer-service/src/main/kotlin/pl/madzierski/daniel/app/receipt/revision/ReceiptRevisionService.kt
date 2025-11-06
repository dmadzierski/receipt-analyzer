package pl.madzierski.daniel.app.receipt.revision

import org.springframework.stereotype.Service
import pl.madzierski.daniel.app.receipt.ReceiptEntity

@Service
class ReceiptRevisionService(val receiptRevisionRepository: ReceiptRevisionRepository) {

    fun saveDefaultReceiptRevision(receipt: ReceiptEntity): ReceiptRevisionEntity =
        this.save(
            ReceiptRevisionEntity(
                null,
                null,
                null,
                receipt,
                mutableListOf(),
                mutableListOf(),
                null,
                null,
                null,
                true
            )
        )

    private fun save(receiptRevision: ReceiptRevisionEntity): ReceiptRevisionEntity {
        return receiptRevisionRepository.save(receiptRevision)
    }

}