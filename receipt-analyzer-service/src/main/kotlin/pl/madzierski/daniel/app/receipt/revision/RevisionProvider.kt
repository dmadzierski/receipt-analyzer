package pl.madzierski.daniel.app.receipt.revision

import org.springframework.stereotype.Service

@Service
class RevisionProvider(val revisionRepository: ReceiptRevisionRepository) {
    fun getReceiptRevisions(receiptId: String): List<ReceiptRevisionEntity> =
        revisionRepository.findReceiptRevisionEntityByReceiptId(receiptId)

}