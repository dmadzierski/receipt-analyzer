package pl.madzierski.daniel.app.common.model.util

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class PdfUtil {

    companion object {

        fun convertToPng(pdfPath: String): List<String> {
            val images = mutableListOf<String>()
            try {
                val pdfFile = File(pdfPath)
                val pdfName = pdfFile.nameWithoutExtension
                val outputDir = pdfFile.parentFile
                PDDocument.load(pdfFile).use { document ->
                    val renderer = PDFRenderer(document)
                    for (page in 0 until document.numberOfPages) {
                        val image: BufferedImage = renderer.renderImageWithDPI(page, 300F)
                        val outputFilePath = File(outputDir, "${pdfName}_${page + 1}.png")
                        ImageIO.write(image, "png", outputFilePath)
                        images.add(outputFilePath.absolutePath)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return images
        }
    }
}