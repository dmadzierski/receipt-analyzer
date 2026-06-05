package pl.madzierski.daniel.app.receipt.scan_resolver.impl

import net.sourceforge.tess4j.Tesseract
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import pl.madzierski.daniel.app.file_group.file.FileService
import pl.madzierski.daniel.app.receipt.revision.model.ReceiptRevisionResolveData
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategyType
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategyTypeStrategy
import pl.madzierski.daniel.app.receipt.scan_resolver.service.PDFService
import pl.madzierski.daniel.exception.AppRuntimeException
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages
import java.io.File

@Component
class BiedronkaReceiptResolverStrategyType @Autowired constructor(
    val fileService: FileService,
    @Value("\${ocr.tesseract.dataPath}") val tesseractDataPath: String,
    @Value("\${receipt-resolver-strategy.biedronka.version}") val resolverVersion: String,
    val pdfService: PDFService
) : ReceiptResolverStrategyTypeStrategy {

    companion object {
        private val itemPatternRegex =
            Regex("^(?<name>.*)\\s+(?<ptu>[ABC])\\s+(?<amount>\\d+[\\s.]?\\d+)\\s*[xX]\\s+(?<unitPrice>\\d+[.,\\s]?\\d+)\\s(?<totalPrice>\\d+[.,\\s]\\d+)$")
        private val itemPatternWithoutPtuRegex =
            Regex("^(?<name>.*)\\s+(?<amount>\\d+[\\s.]?\\d+)\\s*[xX]\\s+(?<unitPrice>\\d+[.,\\s]?\\d+)\\s(?<totalPrice>\\d+[.,\\s]\\d+)\$")
        private val discountPatternRegex = Regex("^Rabat -(?<discount>\\d+[.,\\s]\\d+)$")
        private val discountedPriceRegex = Regex("^(?<totalPrice>\\d+[.,\\s]\\d+)$")
        private val priceSuffixRegex = Regex("\\d+[.,\\s]?\\d{2}$")
        private val startItemIndexRegex = Regex("Nazwa PTU Ilość Cena Wartość", RegexOption.IGNORE_CASE)
        private val lastItemIndexRegex = Regex("Sprzeda[zż] opodatkowana C.*", RegexOption.IGNORE_CASE)
        private val pageInfoRegex = Regex(".*Strona\\s+\\d+\\s+z\\s+\\d+.*", RegexOption.IGNORE_CASE)
    }

    override fun strategy(): ReceiptResolverStrategyType = ReceiptResolverStrategyType.BIEDRONKA

    override fun execute(
        filePath: String
    ): ReceiptRevisionResolveData {

        val receiptFileList = pdfService.dividePdfFileToImages(filePath).mapIndexed { index, pdfPath ->
            val rawData = extractTextFromImage(pdfPath)
            ReceiptRevisionResolveData.ReceiptRevisionResolveDataFile(
                pdfPath, index, rawData
            )
        }

        val rawDataList: List<String> = receiptFileList.map { it.rawData.split("\n").dropLast(1) }.flatten()
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
        val items = mutableListOf<ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem>()
        for (i in rawItemList.indices) {
            itemPatternRegex.matchEntire(rawItemList[i])?.let { matchResult ->
                items.add(extractItem(items.size + 1, matchResult))
            } ?: itemPatternWithoutPtuRegex.matchEntire(rawItemList[i])?.let { matchResult ->
                items.add(extractItem(items.size + 1, matchResult))
            } ?: discountPatternRegex.matchEntire(rawItemList[i])?.let { matchResult ->
                items.last().discount = matchResult.groups["discount"]?.value?.replace(",", ".")?.toDoubleOrNull()
            } ?: discountedPriceRegex.matchEntire(rawItemList[i])?.let { matchResult ->
                items.last().totalPrice = matchResult.groups["totalPrice"]?.value?.replace(",", ".")?.toDoubleOrNull()
            }
        }
        items.forEachIndexed { index, item ->
            item.position = index + 1
        }
        val a = 10
        return ReceiptRevisionResolveData(resolverVersion, "Biedronka", items, receiptFileList)
    }


    private fun extractItem(
        index: Int, matchResult: MatchResult
    ): ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem {
        return ReceiptRevisionResolveData.ReceiptRevisionResolveDataItem(
            getValueFromGroup(matchResult, "name")?.value?.replace(Regex("(?<=\\d)9(?=\\s|$)"), "g"),
            parseDouble(getValueFromGroup(matchResult, "amount")?.value),
            parseDouble(getValueFromGroup(matchResult, "unitPrice")?.value),
            null,
            parseDouble(getValueFromGroup(matchResult, "totalPrice")?.value),
            index
        )
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