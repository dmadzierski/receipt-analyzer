package pl.madzierski.daniel.receipt.scan_resolver.service;

import lombok.AllArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class PDFService {

    private final String tmpFilePath;

    public List<byte[]> dividePdfFileToImages(byte[] file) {
        try {
            List<byte[]> outputImages = new ArrayList<>();
            try (PDDocument document = PDDocument.load(file)) {
                PDFRenderer renderer = new PDFRenderer(document);
                int numberOfPages = document.getNumberOfPages();
                for (int page = 0; page < numberOfPages; page++) {
                    BufferedImage image = renderer.renderImageWithDPI(page, 300f);
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        ImageIO.write(image, "png", baos);
                        outputImages.add(baos.toByteArray());
                    }
                }
            }
            return outputImages;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}