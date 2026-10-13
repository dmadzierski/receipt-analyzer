package pl.madzierski.daniel.file_group;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;

import java.io.IOException;

@Service
class TikaFileTypeDetector implements FileTypeDetector {

    private static final String TEXT_PLAIN = "text/plain";

    private final Tika tika = new Tika();
    private final ObjectMapper objectMapper = new ObjectMapper().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

    @Override
    public FileType detectFileType(byte[] file) {
        if (file == null || file.length == 0) throw new AppRuntimeException(AppRuntimeExceptionMessages.FILE_EMPTY);
        String mimeType = tika.detect(file);
        if (TEXT_PLAIN.equals(mimeType) && isJson(file)) {
            return FileType.JSON;
        } else {
            return FileType.invoke(mimeType).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.FILE_TYPE_NOT_DETECTED));
        }
    }

    private boolean isJson(byte[] file) {
        try {
            return objectMapper.readTree(file)
                .isContainerNode();
        } catch (IOException e) {
            return false;
        }
    }
}
