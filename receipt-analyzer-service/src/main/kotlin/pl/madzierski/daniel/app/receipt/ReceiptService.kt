package pl.madzierski.daniel.app.receipt

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import pl.madzierski.daniel.app.receipt.model.CreateReceiptReceiptFileResponseBody
import pl.madzierski.daniel.app.receipt.model.CreateReceiptReceiptRevisionResponseBody
import pl.madzierski.daniel.app.receipt.model.CreateReceiptRequestBody
import pl.madzierski.daniel.app.receipt.model.CreateReceiptResponseBody
import pl.madzierski.daniel.app.receipt.receipt_file.ReceiptFileService
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionService
import pl.madzierski.daniel.app.receipt.validator.file_validator.ValidateReceiptService
import pl.madzierski.daniel.security.SecurityUtils
import java.time.LocalDateTime


@Service
class ReceiptService constructor(
    val receiptRepository: ReceiptRepository,
    val validateReceiptService: ValidateReceiptService,
    val receiptRevisionService: ReceiptRevisionService,
    val receiptFileService: ReceiptFileService,
) {


    fun addReceipt(file: MultipartFile, body: CreateReceiptRequestBody?): CreateReceiptResponseBody {
        validateReceiptService.validate(file)
        val savedReceipt = this.saveReceipt(createReceipt(body))
        val savedReceiptRevision = receiptRevisionService.createAndSaveRevision(savedReceipt)
        val savedReceiptFile = receiptFileService.createAndSave(savedReceiptRevision, file)

        return CreateReceiptResponseBody(
            savedReceipt.id, savedReceipt.name, savedReceipt.description, listOf(
                CreateReceiptReceiptRevisionResponseBody(
                    savedReceiptRevision.id,
                    savedReceiptRevision.revision,
                    savedReceiptRevision.resolver.name,
                    listOf(CreateReceiptReceiptFileResponseBody(savedReceiptFile.id, savedReceiptFile.path))
                )
            )
        )
    }

    private fun createReceipt(body: CreateReceiptRequestBody?) = ReceiptEntity(
        body?.name.takeUnless { it.isNullOrBlank() } ?: LocalDateTime.now().toString(),
        body?.description,
        SecurityUtils.getCurrentUserSub(),
        null
    )

    private fun saveReceipt(receipt: ReceiptEntity): ReceiptEntity {
        return receiptRepository.save(receipt)
    }

}
