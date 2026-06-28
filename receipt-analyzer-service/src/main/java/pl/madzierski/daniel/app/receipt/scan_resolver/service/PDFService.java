package pl.madzierski.daniel.app.receipt.scan_resolver.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class PDFService {

    private final String tmpFilePath;

    public PDFService(@Value("${pdf-service.tmp-dir}") String tmpFilePath) {
        this.tmpFilePath = tmpFilePath;
    }

    public List<String> dividePdfFileToImages(String pdfFilePath) {
        if (pdfFilePath == null || pdfFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Requirement failed");
        }
        try {
            File pdfFile = new File(pdfFilePath);
            List<String> outputPaths = new ArrayList<>();
            try (PDDocument document = PDDocument.load(pdfFile)) {
                PDFRenderer renderer = new PDFRenderer(document);
                int numberOfPages = document.getNumberOfPages();
                for (int page = 0; page < numberOfPages; page++) {
                    BufferedImage image = renderer.renderImageWithDPI(page, 300f);
                    File outputFilePath = new File(tmpFilePath + "/" + UUID.randomUUID() + "/" + page + ".png");
                    outputFilePath.getParentFile().mkdirs();
                    ImageIO.write(image, "png", outputFilePath);
                    outputPaths.add(outputFilePath.getAbsolutePath());
                }
            }
            return outputPaths;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}