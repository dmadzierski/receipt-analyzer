package pl.madzierski.daniel.app.file_group.file

import org.springframework.core.io.FileSystemResource
import org.springframework.stereotype.Service
import pl.madzierski.daniel.app.file_group.FileType
import pl.madzierski.daniel.app.receipt.ReceiptEntity
import pl.madzierski.daniel.exception.AppRuntimeException
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages
import java.io.File

@Service
class FileService(private val fileRepository: FileRepository) {


    fun save(receiptFile: FileEntity): FileEntity = fileRepository.save(receiptFile)


    fun getFileReceipt(receiptFileId: String): FileSystemResource {
        val file = File(
            fileRepository.findById(receiptFileId)
                .orElseThrow { AppRuntimeException(AppRuntimeExceptionMessages.FILE_RECEIPT_NOT_FOUND) }.path
                ?: throw AppRuntimeException(AppRuntimeExceptionMessages.FILE_RECEIPT_PATH_NOT_FOUND)
        )

        if (!file.exists()) throw AppRuntimeException(AppRuntimeExceptionMessages.FILE_RECEIPT_FILE_NOT_FOUND)

        return FileSystemResource(file)
    }
}