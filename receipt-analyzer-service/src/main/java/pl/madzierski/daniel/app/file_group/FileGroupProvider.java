package pl.madzierski.daniel.app.file_group;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.madzierski.daniel.app.file_group.file.FileEntity;
import pl.madzierski.daniel.app.file_group.file.FileProvider;
import pl.madzierski.daniel.app.receipt.ReceiptEntity;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.security.SecurityUtils;

import java.util.HashSet;

@Service
@AllArgsConstructor
public class FileGroupProvider {

    private final FileGroupRepository fileGroupRepository;
    private final FileProvider fileProvider;

    @Transactional
    public FileGroupEntity saveReceiptFile(ReceiptEntity receipt, MultipartFile file) {
        FileGroupEntity fileGroupEntity = new FileGroupEntity(FileType.PDF, receipt, true, new HashSet<>());
        fileGroupEntity = fileGroupRepository.save(fileGroupEntity);

        FileEntity fileEntity = new FileEntity(fileGroupEntity, null, null, 0);
        fileGroupEntity.getFiles().add(fileEntity);
        fileEntity = fileProvider.save(fileEntity);

        String fileExtension = getExtension(file.getContentType());
        String pathInString = createPath(SecurityUtils.getCurrentUserSub(), fileEntity, fileGroupEntity, fileExtension, receipt);

        fileProvider.saveFile(pathInString, file);
        fileEntity.setPath(pathInString);
        fileProvider.save(fileEntity);

        return fileGroupRepository.save(fileGroupEntity);
    }

    private String getExtension(String contentType) {
        if (MediaType.APPLICATION_PDF_VALUE.equals(contentType)) {
            return "pdf";
        }
        throw new AppRuntimeException(AppRuntimeExceptionMessages.UNHANDLED_MEDIA_TYPE);
    }

    public String createPath(
            String currentUserSub,
            FileEntity file,
            FileGroupEntity fileEntity,
            String fileExtension,
            ReceiptEntity receipt
    ) {
        return String.format("/app/uploads/user/%s/receipts/%s/file_group/%s/file/%s.%s",
                currentUserSub, receipt.getId(), fileEntity.getId(), file.getId(), fileExtension);
    }

    public FileGroupEntity save(FileGroupEntity fileGroupEntity) {
        return fileGroupRepository.save(fileGroupEntity);
    }

    public String getOriginalPdf(String receiptId) {
        return fileGroupRepository.findFirstOriginalPdf(receiptId);
    }
}