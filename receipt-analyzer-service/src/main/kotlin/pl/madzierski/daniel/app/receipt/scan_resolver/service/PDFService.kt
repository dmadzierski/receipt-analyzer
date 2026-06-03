package pl.madzierski.daniel.app.receipt.scan_resolver.service

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.madzierski.daniel.app.file_group.FileGroupEntity
import pl.madzierski.daniel.app.file_group.FileGroupProvider
import pl.madzierski.daniel.app.file_group.FileGroupService
import pl.madzierski.daniel.app.file_group.FileType
import pl.madzierski.daniel.app.file_group.file.FileEntity
import pl.madzierski.daniel.app.file_group.file.FileProvider
import pl.madzierski.daniel.app.receipt.ReceiptEntity
import pl.madzierski.daniel.exception.AppRuntimeException
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

@Service
class PDFService(
    val fileGroupProvider: FileGroupProvider,
    val fileProvider: FileProvider,
    private val fileGroupService: FileGroupService
) {

    @Transactional
    fun createImageFileGroup(receiptEntity: ReceiptEntity, pdfFileEntity: FileEntity): FileGroupEntity {
        if (pdfFileEntity.path?.isEmpty() ?: true) {
            throw AppRuntimeException(AppRuntimeExceptionMessages.FILE_PATH_NOT_FOUND)
        }
        val fileGroupEntity = fileGroupProvider.save(FileGroupEntity(FileType.IMAGE, receiptEntity, false, mutableSetOf()))
        try {
            val pdfFile = File(pdfFileEntity.path!!)
            PDDocument.load(pdfFile).use { document ->
                val renderer = PDFRenderer(document)
                for (page in 0 until document.numberOfPages) {
                    val fileEntity = fileProvider.save(FileEntity(fileGroupEntity, null, null, page))
                    val image: BufferedImage = renderer.renderImageWithDPI(page, 300F)
                    val outputFilePath = File(fileGroupProvider.createPath(receiptEntity.userSub, fileEntity, fileGroupEntity,"png", receiptEntity))
                    outputFilePath.parentFile.mkdirs()
                    ImageIO.write(image, "png", outputFilePath)
                    fileEntity.path = outputFilePath.absolutePath
                    fileGroupEntity.files.add(fileEntity)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return fileGroupProvider.save(fileGroupEntity)
    }
}