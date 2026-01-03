package pl.madzierski.daniel.app.receipt.scan_resolver.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pl.madzierski.daniel.app.receipt.ReceiptEntity
import pl.madzierski.daniel.app.receipt.model.OCRHandlingResolver
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionEntity
import pl.madzierski.daniel.app.receipt.revision.receipt_file.ReceiptFileEntity
import pl.madzierski.daniel.app.receipt.scan_resolver.impl.BiedronkaScanResolver

@Service
class ScanReceiptResolverService @Autowired constructor(
    val biedronkaScanResolver: BiedronkaScanResolver
) {

    private fun findBrand(receiptFileEntity: ReceiptFileEntity): OCRHandlingResolver {
        return OCRHandlingResolver.BIEDRONKA
    }

    fun resolve(
        receipt: ReceiptEntity,
        receiptRevision: ReceiptRevisionEntity,
        receiptFile: ReceiptFileEntity
    ): ReceiptRevisionEntity =
        when (findBrand(receiptFile)) {
            OCRHandlingResolver.BIEDRONKA -> biedronkaScanResolver.execute(receipt, receiptRevision, receiptFile)
        }


}