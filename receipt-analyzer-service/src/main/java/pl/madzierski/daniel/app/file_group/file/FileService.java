package pl.madzierski.daniel.app.file_group.file;

import lombok.AllArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;

import java.io.File;

@Service
@AllArgsConstructor
public class FileService {

    private final FileRepository fileRepository;

    public FileSystemResource getFileReceipt(String receiptFileId) {
        FileEntity fileEntity = fileRepository.findById(receiptFileId)
                .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.FILE_RECEIPT_NOT_FOUND));
        String path = fileEntity.getPath();
        if (path == null) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.FILE_RECEIPT_PATH_NOT_FOUND);
        }

        File file = new File(path);

        if (!file.exists()) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.FILE_RECEIPT_FILE_NOT_FOUND);
        }

        return new FileSystemResource(file);
    }
}