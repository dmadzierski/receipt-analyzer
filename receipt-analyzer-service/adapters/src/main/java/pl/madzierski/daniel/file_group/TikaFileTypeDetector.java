package pl.madzierski.daniel.file_group;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

@Service
class TikaFileTypeDetector implements FileTypeDetector {

    private final Tika tika = new Tika();

    @Override
    public FileType detectFileType(byte[] file) {
        if (file == null || file.length == 0) {
            throw new IllegalArgumentException("Empty file content");
        }
        String mimeType = tika.detect(file);
        return FileType.invoke(mimeType)
            .orElseThrow(() -> new IllegalArgumentException("Unsupported content type: " + mimeType));
    }
}