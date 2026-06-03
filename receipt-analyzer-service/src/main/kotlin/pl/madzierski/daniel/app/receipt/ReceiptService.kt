package pl.madzierski.daniel.app.receipt

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import pl.madzierski.daniel.app.file_group.FileGroupProvider
import pl.madzierski.daniel.app.receipt.model.*
import pl.madzierski.daniel.app.receipt.revision.*
import pl.madzierski.daniel.app.receipt.scan_resolver.service.ScanReceiptResolverService
import pl.madzierski.daniel.app.receipt.validator.file_validator.ValidateReceiptService
import pl.madzierski.daniel.security.SecurityUtils
import java.time.LocalDateTime

@Service
class ReceiptService(
    val receiptRepository: ReceiptRepository,
    val receiptRevisionRepository: ReceiptRevisionRepository,
    val receiptRevisionService: ReceiptRevisionService,
    val fileGroupProvider: FileGroupProvider,
    val validateReceiptService: ValidateReceiptService,
    val receiptResolverService: ScanReceiptResolverService,
    val revisionProvider: RevisionProvider,
    @Value("\${receipt.revision.default.version}") val revisionVersion: String,
    @Value("\${receipt.default.brand}") val defaultBrand: String
) {

    fun addReceipt(file: MultipartFile, body: CreateReceiptRequest?): CreateReceiptResponse {
        validateReceiptService.validate(file)
        val receipt = this.saveReceipt(createReceipt(body))
        var receiptRevision = this.createReceiptRevisionEntity(receipt, defaultBrand, ScanResolver.OCR, revisionVersion)
        receiptRevision = this.receiptRevisionService.saveDefaultReceiptRevision(receiptRevision)
        val fileGroup = this.fileGroupProvider.saveReceiptFile(receipt, file)
        receipt.receiptRevisions.add(receiptResolverService.resolve(receipt, receiptRevision, fileGroup))
        return this.saveReceipt(receipt).let { CreateReceiptResponse(it.id, it.name, it.description) }
    }

    private fun createReceipt(body: CreateReceiptRequest?) =
        ReceiptEntity(body?.name.takeUnless { it.isNullOrBlank() } ?: LocalDateTime.now().toString(),
            body?.description,
            SecurityUtils.getCurrentUserSub(),
            mutableSetOf(),
            mutableSetOf())


    private fun saveReceipt(receipt: ReceiptEntity): ReceiptEntity = receiptRepository.save(receipt)

    fun getReceiptList(): GetReceiptListResponse {
        return GetReceiptListResponse(receiptRepository.getReceiptList(SecurityUtils.getCurrentUserSub()))
    }

    fun createReceiptRevisionEntity(
        receipt: ReceiptEntity, brand: String, scanResolver: ScanResolver, revisionVersion: String,
    ): ReceiptRevisionEntity =
        ReceiptRevisionEntity(
            null,
            revisionVersion,
            scanResolver,
            brand,
            receipt,
            mutableSetOf(),
            null,
            null,
            null,
            true,
            false
        )

    @Transactional(readOnly = true)
    fun getReceiptDetails(receiptId: String): GetReceiptDetailsResponse {
        val receiptEntity = receiptRepository.findReceiptEntityById(receiptId)

        val preferredRevisionId = receiptEntity.receiptRevisions.firstOrNull { it.isPreferredRevision == true }?.id
        val revisionEntity = preferredRevisionId?.let { id ->
            receiptRevisionRepository.findReceiptRevisionEntitiesById(id)
        }
        val fileId = fileGroupProvider.getOriginalPdf(receiptEntity.id!!)

        return GetReceiptDetailsResponse.receiptDetailsMapper(receiptEntity, revisionEntity, fileId)
    }

    fun getReceiptRevisions(receiptId: String): List<GetReceiptRevisionsResponse> =
        revisionProvider.getReceiptRevisions(receiptId).map { GetReceiptRevisionsResponse.receiptRevisionMapper(it) }
            .toList()
}

