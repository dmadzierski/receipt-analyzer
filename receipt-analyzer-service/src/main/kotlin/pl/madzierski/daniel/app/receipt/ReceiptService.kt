package pl.madzierski.daniel.app.receipt

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import pl.madzierski.daniel.app.receipt.model.*
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionEntity
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionRepository
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionService
import pl.madzierski.daniel.app.receipt.revision.item.ItemEntity
import pl.madzierski.daniel.app.receipt.revision.receipt_file.ReceiptFileEntity
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
    val receiptResolverService: ScanReceiptResolverService
) {

    companion object {
        private const val DEFAULT_FIRST_REVISION_NUMBER = "1.0"
    }


    fun addReceipt(file: MultipartFile, body: CreateReceiptRequest?): CreateReceiptResponse {
        validateReceiptService.validate(file)
        var receipt = this.saveReceipt(createReceipt(body))
        val receiptRevision = this.receiptRevisionService.saveDefaultReceiptRevision(receipt)
        val receiptFile = this.receiptFileService.saveReceiptFile(receiptRevision, file)
        receipt.receiptRevisions.add(receiptResolverService.resolve(receipt, receiptRevision, receiptFile))
        receipt = this.saveReceipt(receipt)
        return CreateReceiptResponse(receipt.id, receipt.name, receipt.description)
    }

    private fun createReceipt(body: CreateReceiptRequest?) =
        ReceiptEntity(body?.name.takeUnless { it.isNullOrBlank() } ?: LocalDateTime.now().toString(),
            body?.description,
            SecurityUtils.getCurrentUserSub(),
            mutableListOf())


    private fun saveReceipt(receipt: ReceiptEntity): ReceiptEntity = receiptRepository.save(receipt)

    fun getReceiptList(): GetReceiptListResponse {
        return GetReceiptListResponse(receiptRepository.getReceiptList(SecurityUtils.getCurrentUserSub()))
    }

    fun getReceiptDetails(receiptId: String): GetReceiptDetailsResponse {
        val receiptEntity = receiptRepository.findReceiptEntityById(receiptId)

        return receiptEntity.receiptRevisions.first { it.preferredRevision!! }.id?.let {
            receiptRevisionRepository.findReceiptRevisionEntitiesById(it)
        }.let {
            GetReceiptDetailsResponse(
                receiptEntity.id,
                receiptEntity.name,
                receiptEntity.description,
                receiptRevisionMapper(receiptEntity.receiptRevisions)
            )
        }
    }

    private fun receiptRevisionMapper(receiptRevisions: List<ReceiptRevisionEntity>): List<GetReceiptDetailsRevisionResponse> =
        if (receiptRevisions.isNotEmpty()) receiptRevisions.map {
            GetReceiptDetailsRevisionResponse(
                it.id,
                it.resolver,
                it.createdDate
            )
        }
        else emptyList()

    private fun receiptRevisionDetailsMapper(receiptRevision: ReceiptRevisionEntity): GetReceiptDetailsRevisionDetailsResponse =
        GetReceiptDetailsRevisionDetailsResponse(
            receiptRevision.id!!,
            receiptRevision.brand,
            receiptRevisionDetailsReceiptFilesMapper(receiptRevision.receiptFiles),
            receiptRevisionDetailsReceiptItemMapper(receiptRevision.items),
            receiptRevision.totalPrice,
            receiptRevision.payingDate,
            receiptRevision.address
        )

    private fun receiptRevisionDetailsReceiptFilesMapper(receiptFiles: List<ReceiptFileEntity>): List<GetReceiptDetailsFileResponse> =
        receiptFiles.map { GetReceiptDetailsFileResponse(it.id!!) }

    private fun receiptRevisionDetailsReceiptItemMapper(receiptItems: List<ItemEntity>): List<GetReceiptDetailsItemResponse> =
        receiptItems.map {
            GetReceiptDetailsItemResponse(
                it.id!!, it.name, it.vat, it.amount, it.unitPrice, it.discount, it.totalPrice
            )
        }

}
