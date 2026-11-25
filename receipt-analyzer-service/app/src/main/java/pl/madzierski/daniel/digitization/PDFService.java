package pl.madzierski.daniel.digitization;

import lombok.AllArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class PDFService {

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
        } catch (IOException e) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.CAN_NOT_RESOLVE_RECEIPT, e);
        }
    }
}