package pl.madzierski.daniel.app.file_group.file;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;

import java.io.File;
import java.io.IOException;

@Service
@AllArgsConstructor
public class FileProvider {

    private final FileRepository fileRepository;

    public void saveFile(String path, MultipartFile file) {
        try {
            File destFile = new File(path);
            if (destFile.getParentFile() != null) {
                destFile.getParentFile().mkdirs();
            }
            file.transferTo(destFile);
        } catch (IOException e) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.UNHANDLED_FILE_TYPE);
        }
    }

    public FileEntity save(FileEntity fileEntity) {
        return fileRepository.save(fileEntity);
    }
}