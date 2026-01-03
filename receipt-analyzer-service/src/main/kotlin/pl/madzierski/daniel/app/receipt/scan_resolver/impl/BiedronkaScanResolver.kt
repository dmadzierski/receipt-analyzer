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
            Regex("^(?<name>.*)\\s+(?<ptu>[ABC])\\s+(?<amount>\\d+[\\s.]?\\d+)\\s*[xX]\\s+(?<unitPrice>\\d+[.,\\s]?\\d+)\\s(?<totalPrice>\\d+[.,\\s]\\d+)$")
        val itemPatternWithoutPtuRegex =
            Regex("^(?<name>.*)\\s+(?<amount>\\d+[\\s.]?\\d+)\\s*[xX]\\s+(?<unitPrice>\\d+[.,\\s]?\\d+)\\s(?<totalPrice>\\d+[.,\\s]\\d+)\$")
        val discountPatternRegex = Regex("^Rabat -(?<discount>\\d+[.,\\s]\\d+)$")
        val discountedPriceRegex = Regex("^(?<totalPrice>\\d+[.,\\s]\\d+)$")
        val priceSuffixRegex = Regex("\\d+[.,\\s]?\\d{2}$")
        val startItemIndexRegex = Regex("Nazwa PTU Ilość Cena Wartość", RegexOption.IGNORE_CASE)
        val lastItemIndexRegex = Regex("Sprzeda[zż] opodatkowana C.*", RegexOption.IGNORE_CASE)
        val pageInfoRegex = Regex(".*Strona\\s+\\d+\\s+z\\s+\\d+.*", RegexOption.IGNORE_CASE)
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

        if (startItemsIndex == -1 || lastIItemIndex == -1 || lastIItemIndex < startItemsIndex) throw AppRuntimeException(
            AppRuntimeExceptionMessages.CAN_NOT_RESOLVER_RECEIPT
        )

        var rawItemList = rawDataList.subList(startItemsIndex, lastIItemIndex)

        val mergedItemList = mutableListOf<String>()
        var nameBuffer = ""

        for (line in rawItemList) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue
            if (pageInfoRegex.matches(trimmedLine)) continue
            if (priceSuffixRegex.containsMatchIn(trimmedLine)) {
                if (nameBuffer.isNotEmpty()) {
                    val cleanLine = trimmedLine.replace(Regex("^.*?(?=\\d[.,]\\d{3}|[ABC]\\s)"), "")
                    mergedItemList.add("$nameBuffer $cleanLine")
                    nameBuffer = ""
                } else {
                    mergedItemList.add(trimmedLine)
                }
            } else {
                nameBuffer = trimmedLine
            }
        }
        rawItemList = mergedItemList

        for (i in rawItemList.indices) {
            itemPatternRegex.matchEntire(rawItemList[i])?.let { matchResult ->
                saveResult(receiptRevision, matchResult)
            } ?: itemPatternWithoutPtuRegex.matchEntire(rawItemList[i])?.let { matchResult ->
                saveResult(receiptRevision, matchResult)
            }
            ?: discountPatternRegex.matchEntire(rawItemList[i])?.let { matchResult ->
                receiptRevision.items.last().discount =
                    matchResult.groups["discount"]?.value?.replace(",", ".")?.toDoubleOrNull()
            } ?: discountedPriceRegex.matchEntire(rawItemList[i])?.let { matchResult ->
                receiptRevision.items.last().totalPrice =
                    matchResult.groups["totalPrice"]?.value?.replace(",", ".")?.toDoubleOrNull()
            }
        }

        itemService.saveAll(receiptRevision.items).toMutableSet().also { receiptRevision.items = it }


        return receiptRevision
    }

    private fun saveResult(
        receiptRevision: ReceiptRevisionEntity,
        matchResult: MatchResult
    ): Boolean {
        val itemEntity = ItemEntity(
            receiptRevision,
            getValueFromGroup(matchResult, "name")?.value?.replace(Regex("(?<=\\d)9(?=\\s|$)"), "g"),
            getValueFromGroup(matchResult, "ptu")?.value,
            parseDouble(getValueFromGroup(matchResult, "amount")?.value),
            parseDouble(getValueFromGroup(matchResult, "unitPrice")?.value),
            null,
            parseDouble(getValueFromGroup(matchResult, "totalPrice")?.value),
            (receiptRevision.items.size + 1)
        )
        return receiptRevision.items.add(itemEntity)
    }

    private fun getValueFromGroup(matchResult: MatchResult, group: String): MatchGroup? {
        return try {
            matchResult.groups[group]
        } catch (_: Exception) {
            null
        }
    }

    private fun parseDouble(valStr: String?): Double? {
        return valStr?.replace(" ", ".")?.replace(",", ".")?.toDoubleOrNull()
    }

    fun extractTextFromImage(imagePath: String): String = Tesseract().apply {
        setDatapath(tesseractDataPath)
        setLanguage("pol+eng")
        setPageSegMode(6)
        setOcrEngineMode(0)
    }.doOCR(File(imagePath))
}