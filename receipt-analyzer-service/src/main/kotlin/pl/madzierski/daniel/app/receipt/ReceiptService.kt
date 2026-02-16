package pl.madzierski.daniel.app.receipt

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import pl.madzierski.daniel.app.receipt.model.*
import pl.madzierski.daniel.app.receipt.revision.*
import pl.madzierski.daniel.app.receipt.revision.receipt_file.ReceiptFileService
import pl.madzierski.daniel.app.receipt.scan_resolver.service.ScanReceiptResolverService
import pl.madzierski.daniel.app.receipt.validator.file_validator.ValidateReceiptService
import pl.madzierski.daniel.security.SecurityUtils
import java.time.LocalDateTime


@Service
class ReceiptService(
    val receiptRepository: ReceiptRepository,
    val receiptRevisionRepository: ReceiptRevisionRepository,
    val receiptRevisionService: ReceiptRevisionService,
    val receiptFileService: ReceiptFileService,
    val validateReceiptService: ValidateReceiptService,
    val receiptResolverService: ScanReceiptResolverService,
    val revisionProvider: RevisionProvider,
    @Value("\${receipt.revision.default.version}") val revisionVersion: String
) {

    fun addReceipt(file: MultipartFile, body: CreateReceiptRequest?): CreateReceiptResponse {
        validateReceiptService.validate(file)
        var receipt = this.saveReceipt(createReceipt(body))
        var receiptRevision = this.createReceiptRevisionEntity(receipt, "Biedronka", ScanResolver.OCR, revisionVersion)
        receiptRevision = this.receiptRevisionService.saveDefaultReceiptRevision(receiptRevision)
        val receiptFile = this.receiptFileService.saveReceiptFile(receiptRevision, file)
        receipt.receiptRevisions.add(receiptResolverService.resolve(receipt, receiptRevision, receiptFile))
        receipt = this.saveReceipt(receipt)
        return CreateReceiptResponse(receipt.id, receipt.name, receipt.description)
    }

    private fun createReceipt(body: CreateReceiptRequest?) =
        ReceiptEntity(body?.name.takeUnless { it.isNullOrBlank() } ?: LocalDateTime.now().toString(),
            body?.description,
            SecurityUtils.getCurrentUserSub(),
            mutableSetOf())


    private fun saveReceipt(receipt: ReceiptEntity): ReceiptEntity = receiptRepository.save(receipt)

    fun getReceiptList(): GetReceiptListResponse {
        return GetReceiptListResponse(receiptRepository.getReceiptList(SecurityUtils.getCurrentUserSub()))
    }

    fun createReceiptRevisionEntity(
        receipt: ReceiptEntity, brand: String, scanResolver: ScanResolver, revisionVersion: String,
    ): ReceiptRevisionEntity = ReceiptRevisionEntity(
        null,
        revisionVersion,
        scanResolver,
        brand,
        receipt,
        mutableSetOf(),
        mutableSetOf(),
        null,
        null,
        null,
        true,
        false,
    )

    fun getReceiptDetails(receiptId: String): GetReceiptDetailsResponse {
        val receiptEntity = receiptRepository.findReceiptEntityById(receiptId)

        val preferredRevisionId = receiptEntity.receiptRevisions.firstOrNull { it.isPreferredRevision == true }?.id
        val revisionEntity = preferredRevisionId?.let { id ->
            receiptRevisionRepository.findReceiptRevisionEntitiesById(id)
        }

        return GetReceiptDetailsResponse.receiptDetailsMapper(receiptEntity, revisionEntity)
    }

    fun getReceiptRevisions(receiptId: String): List<GetReceiptRevisionsResponse> =
        revisionProvider.getReceiptRevisions(receiptId).map { GetReceiptRevisionsResponse.receiptRevisionMapper(it) }
            .toList()
}
