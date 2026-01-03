package pl.madzierski.daniel.app.receipt

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import pl.madzierski.daniel.app.receipt.model.CreateReceiptRequest
import pl.madzierski.daniel.app.receipt.model.CreateReceiptResponse
import pl.madzierski.daniel.app.receipt.model.GetReceiptDetailsResponse
import pl.madzierski.daniel.app.receipt.model.GetReceiptListResponse
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionRepository
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionService
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
            mutableSetOf())


    private fun saveReceipt(receipt: ReceiptEntity): ReceiptEntity = receiptRepository.save(receipt)

    fun getReceiptList(): GetReceiptListResponse {
        return GetReceiptListResponse(receiptRepository.getReceiptList(SecurityUtils.getCurrentUserSub()))
    }

    fun getReceiptDetails(receiptId: String): GetReceiptDetailsResponse {
        val receiptEntity = receiptRepository.findReceiptEntityById(receiptId)
        return receiptEntity.receiptRevisions.first { true == it.preferredRevision }.id?.let {
            receiptRevisionRepository.findReceiptRevisionEntitiesById(it)
        }.let {
            GetReceiptDetailsResponse.receiptDetailsMapper(receiptEntity, it)
        }
    }
}
