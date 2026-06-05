package pl.madzierski.daniel.app.receipt.scan_resolver.service

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO

@Service
class PDFService(
    @Value("\${pdf-service.tmp-dir}") private val tmpFilePath: String
) {

    fun dividePdfFileToImages(pdfFilePath: String): List<String> {
        require(pdfFilePath.isNotBlank())
        return try {
            val pdfFile = File(pdfFilePath)
            val document = PDDocument.load(pdfFile)
            val renderer = PDFRenderer(document)
            (0 until document.numberOfPages).map { page ->
                val image: BufferedImage = renderer.renderImageWithDPI(page, 300F)
                val outputFilePath = File("$tmpFilePath/${UUID.randomUUID()}/${page}.png")
                outputFilePath.parentFile.mkdirs()
                ImageIO.write(image, "png", outputFilePath)
                outputFilePath.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}