package pl.madzierski.daniel.app.receipt

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import pl.madzierski.daniel.app.file_group.FileGroupProvider
import pl.madzierski.daniel.app.receipt.model.*
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionEntity
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionRepository
import pl.madzierski.daniel.app.receipt.revision.RevisionProvider
import pl.madzierski.daniel.app.receipt.revision.item.ItemEntity
import pl.madzierski.daniel.app.receipt.revision.model.ReceiptRevisionResolveData
import pl.madzierski.daniel.app.receipt.scan_resolver.service.ReceiptResolverService
import pl.madzierski.daniel.app.receipt.validator.file_validator.ValidateReceiptService
import pl.madzierski.daniel.security.SecurityUtils
import java.time.LocalDateTime

@Service
class ReceiptService(
    val receiptRepository: ReceiptRepository,
    val receiptRevisionRepository: ReceiptRevisionRepository,
    val fileGroupProvider: FileGroupProvider,
    val validateReceiptService: ValidateReceiptService,
    val receiptResolverService: ReceiptResolverService,
    val revisionProvider: RevisionProvider,
) {

    @Transactional
    fun addReceipt(file: MultipartFile, body: CreateReceiptRequest): CreateReceiptResponse {
        validateReceiptService.validate(file)
        val receipt = this.saveReceipt(createReceipt(body))
        val fileGroup = this.fileGroupProvider.saveReceiptFile(receipt, file)
        val revisionData = receiptResolverService.resolve(
            fileGroup.files.first().path!!, body.strategy
        )
        receipt.addRevision(mapToReceiptRevisionEntity(body, revisionData))
        return this.saveReceipt(receipt).let { CreateReceiptResponse(it.id, it.name, it.description) }
    }

    private fun createReceipt(body: CreateReceiptRequest?) =
        ReceiptEntity(body?.name.takeUnless { it.isNullOrBlank() } ?: LocalDateTime.now().toString(),
            body?.description,
            SecurityUtils.getCurrentUserSub(),
            mutableSetOf(),
            mutableSetOf())

    private fun mapToReceiptRevisionEntity(
        body: CreateReceiptRequest, revisionData: ReceiptRevisionResolveData
    ): ReceiptRevisionEntity {
        val revisionEntity = ReceiptRevisionEntity(
            body.name,
            revisionData.revisionVersion,
            body.strategy,
            revisionData.brand,
            null,
            mutableSetOf(),
            revisionData.items.sumOf { it.totalPrice ?: 0.0 },
            LocalDateTime.now().toString(),
            null,
            true,
            false,
        )
        val items = revisionData.items.map {
            ItemEntity(null, it.name, it.amount, it.unitPrice, it.discount, it.totalPrice, it.position)
        }.toMutableSet()
        revisionEntity.addItems(items)
        return revisionEntity
    }

    private fun saveReceipt(receipt: ReceiptEntity): ReceiptEntity = receiptRepository.save(receipt)

    fun getReceiptList(): GetReceiptListResponse {
        return GetReceiptListResponse(receiptRepository.getReceiptList(SecurityUtils.getCurrentUserSub()))
    }


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

