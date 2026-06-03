package pl.madzierski.daniel.app.file_group

import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import pl.madzierski.daniel.app.file_group.file.FileEntity
import pl.madzierski.daniel.app.file_group.file.FileProvider
import pl.madzierski.daniel.app.receipt.ReceiptEntity
import pl.madzierski.daniel.exception.AppRuntimeException
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages
import pl.madzierski.daniel.security.SecurityUtils

@Service
class FileGroupProvider(private val fileGroupRepository: FileGroupRepository, private val fileProvider: FileProvider) {

    @Transactional
    fun saveReceiptFile(receipt: ReceiptEntity, file: MultipartFile): FileGroupEntity {
        var fileGroupEntity = FileGroupEntity(FileType.PDF, receipt, true, mutableSetOf())
        fileGroupEntity = fileGroupRepository.save(fileGroupEntity)
        var fileEntity = FileEntity(fileGroupEntity, null, null, 0)
        fileGroupEntity.files.add(fileEntity)
        fileEntity = fileProvider.save(fileEntity)
        val fileExtension = getExtension(file.contentType)
        val pathInString =
            createPath(SecurityUtils.getCurrentUserSub(), fileEntity, fileGroupEntity, fileExtension, receipt)
        fileProvider.saveFile(pathInString, file)
        fileEntity.path = pathInString
        fileProvider.save(fileEntity)
        val save = fileGroupRepository.save(fileGroupEntity)
        return save
    }


    private fun getExtension(contentType: String?): String {
        return when (contentType) {
            MediaType.APPLICATION_PDF_VALUE -> "pdf"
            else -> throw AppRuntimeException(AppRuntimeExceptionMessages.UNHANDLED_MEDIA_TYPE)
        }
    }

    fun createPath(
        currentUserSub: String,
        file: FileEntity,
        fileEntity: FileGroupEntity,
        fileExtension: String,
        receipt: ReceiptEntity
    ): String =
        "/app/uploads/user/$currentUserSub/receipts/${receipt.id}/file_group/${fileEntity.id}/file/${file.id}.${fileExtension}"

    fun save(fileGroupEntity: FileGroupEntity): FileGroupEntity = fileGroupRepository.save(fileGroupEntity)

    fun getOriginalPdf(receiptId: String): String {
        return fileGroupRepository.findFirstOriginalPdf(receiptId)
    }

}