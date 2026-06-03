package pl.madzierski.daniel.app.receipt.scan_resolver

import pl.madzierski.daniel.app.file_group.FileGroupEntity
import pl.madzierski.daniel.app.receipt.ReceiptEntity
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionEntity
import pl.madzierski.daniel.app.file_group.file.FileEntity

interface ScanResolverStrategy {

    fun execute(
        receipt: ReceiptEntity,
        receiptRevision: ReceiptRevisionEntity,
        fileGroupEntity: FileGroupEntity,
    ): ReceiptRevisionEntity
}