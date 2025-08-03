package pl.madzierski.daniel.app.receipt.revision

import org.springframework.stereotype.Service
import pl.madzierski.daniel.app.receipt.ReceiptEntity

@Service
class ReceiptRevisionService(val receiptRevisionRepository: ReceiptRevisionRepository) {

    companion object {
        private const val DEFAULT_REVISION_NUMBER = "1.0"
        private val DEFAULT_REVISION_RESOLVER = ScanResolver.USER
    }

    fun createAndSaveRevision(receipt: ReceiptEntity): ReceiptRevisionEntity {
        val revision = ReceiptRevisionEntity(
            DEFAULT_REVISION_NUMBER,
            DEFAULT_REVISION_RESOLVER,
            null,
            receipt,
            null,
            null,
            null
        )
        return save(revision)
    }

    private fun save(receiptRevision: ReceiptRevisionEntity): ReceiptRevisionEntity {
        return receiptRevisionRepository.save(receiptRevision)
    }


}