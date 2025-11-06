package pl.madzierski.daniel.app.receipt.scan_resolver

import pl.madzierski.daniel.app.receipt.ReceiptEntity
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionEntity
import pl.madzierski.daniel.app.receipt.revision.receipt_file.ReceiptFileEntity

interface ScanResolverStrategy {

    fun execute(
        receipt: ReceiptEntity,
        receiptRevision: ReceiptRevisionEntity,
        receiptFileEntity: ReceiptFileEntity
    ): ReceiptRevisionEntity
}