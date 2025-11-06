package pl.madzierski.daniel.app.receipt.revision.receipt_file

import org.springframework.core.io.FileSystemResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionEntity
import pl.madzierski.daniel.exception.AppRuntimeException
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages
import pl.madzierski.daniel.security.SecurityUtils
import java.io.File
import java.io.IOException
import java.util.*

@Service
class ReceiptFileService(private val receiptFileRepository: ReceiptFileRepository) {

    fun saveReceiptFile(receiptRevision: ReceiptRevisionEntity, file: MultipartFile): ReceiptFileEntity {

        val receiptFile = ReceiptFileEntity(receiptRevision, null, true)
        val fileExtension = getExtension(file.contentType)
        val pathInString = createPath(SecurityUtils.getCurrentUserSub(), receiptFile, fileExtension)
        saveFile(pathInString, file)
        val savedReceiptFileWithPath = ReceiptFileEntity(receiptFile.receiptRevision, pathInString)

        return this.save(savedReceiptFileWithPath)
    }

    fun save(receiptFile: ReceiptFileEntity): ReceiptFileEntity = receiptFileRepository.save(receiptFile)

    fun saveReceiptFile(receiptRevision: ReceiptRevisionEntity, path: String, isOriginal: Boolean): ReceiptFileEntity =
        this.save(ReceiptFileEntity(receiptRevision, path, isOriginal))

    private fun createPath(currentUserSub: String, receiptFile: ReceiptFileEntity, fileExtension: String) =
        "/app/uploads/user/$currentUserSub/receipts/${receiptFile.receiptRevision.receipt.id}/revision/${receiptFile.receiptRevision.id}/${UUID.randomUUID()}.${fileExtension}"


    private fun saveFile(path: String, file: MultipartFile) {
        try {
            val destFile = File(path)
            destFile.parentFile?.mkdirs()
            file.transferTo(destFile)
        } catch (e: IOException) {
            throw e
        }
    }

    private fun getExtension(contentType: String?): String {
        return when (contentType) {
            MediaType.APPLICATION_PDF_VALUE -> "pdf"
            else -> throw AppRuntimeException(AppRuntimeExceptionMessages.UNHANDLED_MEDIA_TYPE)
        }
    }

    fun getFileReceipt(receiptFileId: String): FileSystemResource {
        val file = File(
            receiptFileRepository.findById(receiptFileId)
                .orElseThrow { AppRuntimeException(AppRuntimeExceptionMessages.FILE_RECEIPT_NOT_FOUND) }.path
                ?: throw AppRuntimeException(AppRuntimeExceptionMessages.FILE_RECEIPT_PATH_NOT_FOUND)
        )

        if (!file.exists()) throw AppRuntimeException(AppRuntimeExceptionMessages.FILE_RECEIPT_FILE_NOT_FOUND)

        return FileSystemResource(file)
    }
}