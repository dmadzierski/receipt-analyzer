package pl.madzierski.daniel.app.receipt.scan_resolver.impl

import net.sourceforge.tess4j.Tesseract
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import pl.madzierski.daniel.app.common.model.util.PdfUtil
import pl.madzierski.daniel.app.receipt.ReceiptEntity
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionEntity
import pl.madzierski.daniel.app.receipt.revision.item.ItemEntity
import pl.madzierski.daniel.app.receipt.revision.item.ItemService
import pl.madzierski.daniel.app.receipt.revision.receipt_file.ReceiptFileEntity
import pl.madzierski.daniel.app.receipt.revision.receipt_file.ReceiptFileService
import pl.madzierski.daniel.app.receipt.scan_resolver.ScanResolverStrategy
import pl.madzierski.daniel.exception.AppRuntimeException
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages
import java.io.File

@Component
class BiedronkaScanResolver @Autowired constructor(
    val receiptFileService: ReceiptFileService,
    @Value("\${ocr.tesseract.dataPath}") val tesseractDataPath: String,
    val itemService: ItemService,
) : ScanResolverStrategy {

    companion object {
        val itemPatternRegex =
            Regex("^(?<name>.*)\\s(?<vat>[ABC])\\s(?<amount>\\d+\\.\\d+)\\s[x]\\s(?<unitPrice>\\d+,\\d+)\\s(?<totalPrice>\\d+,\\d+)\$")
        val discountPatternRegex = Regex("^Rabat -(?<discount>\\d+,\\d+)$")
        val discountedPriceRegex = Regex("^(?<totalPrice>\\d+,\\d+)$")
        val startItemIndexRegex = Regex("Nazwa PTU Ilość Cena Wartość")
        val lastItemIndexRegex = Regex("Sprzeda[zż] opodatkowana C.*")
    }

    override fun execute(
        receipt: ReceiptEntity, receiptRevision: ReceiptRevisionEntity, receiptFileEntity: ReceiptFileEntity
    ): ReceiptRevisionEntity {

        val receiptFileList = receiptFileEntity.path?.let {
            PdfUtil.convertToPng(it).map { receiptFileService.saveReceiptFile(receiptRevision, it, false) }
        }?.map {
            val rawData = extractTextFromImage(it.path!!)
            it.rawData = rawData
            receiptFileService.save(it)
        }

        val rawDataList: List<String> =
            receiptFileList?.mapNotNull { it.rawData?.split("\n")?.dropLast(1) }?.flatten()!!
        val startItemsIndex = (rawDataList.indexOfFirst { startItemIndexRegex.containsMatchIn(it) } + 1)
        val lastIItemIndex = rawDataList.indexOfFirst { lastItemIndexRegex.containsMatchIn(it) }

        if (startItemsIndex == -1 || lastIItemIndex == -1 || lastIItemIndex < startItemsIndex)
            throw AppRuntimeException(AppRuntimeExceptionMessages.CAN_NOT_RESOLVER_RECEIPT)

        val rawItemList = rawDataList.subList(startItemsIndex, lastIItemIndex)

        for (i in rawItemList.indices) {
            itemPatternRegex.matchEntire(rawDataList[i])?.let { matchResult ->
                val itemEntity = ItemEntity(
                    receiptRevision,
                    matchResult.groups["name"]?.value,
                    matchResult.groups["vat"]?.value,
                    matchResult.groups["amount"]?.value?.toDoubleOrNull(),
                    matchResult.groups["unitPrice"]?.value?.replace(",", ".")?.toDoubleOrNull(),
                    null,
                    matchResult.groups["totalPrice"]?.value?.replace(",", ".")?.toDoubleOrNull(),
                    (receiptRevision.items.size + 1)
                )
                receiptRevision.items.add(itemEntity)
            } ?: discountPatternRegex.matchEntire(rawDataList[i])?.let { matchResult ->
                receiptRevision.items.last().discount =
                    matchResult.groups["discount"]?.value?.replace(",", ".")?.toDoubleOrNull()
            } ?: discountedPriceRegex.matchEntire(rawDataList[i])?.let { matchResult ->
                receiptRevision.items.last().totalPrice =
                    matchResult.groups["totalPrice"]?.value?.replace(",", ".")?.toDoubleOrNull()
            }
        }

        itemService.saveAll(receiptRevision.items).toMutableSet().also { receiptRevision.items = it }


        return receiptRevision
    }

    fun extractTextFromImage(imagePath: String): String = Tesseract().apply {
        setDatapath(tesseractDataPath)
        setLanguage("pol+eng")
    }.doOCR(File(imagePath))
}