package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.madzierski.daniel.file_group.model.FileDto;
import pl.madzierski.daniel.file_group.model.FileGroupDto;
import pl.madzierski.daniel.receipt.model.ReceiptDto;
import pl.madzierski.daniel.receipt.model.ReceiptQueryEntity;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FileFacade {

    public static final String FILE_PATH_TEMPLATE = "/app/uploads/user/%s/receipts/%s/file_group/%s/file/%s.%s";
    private final FileRepository fileRepository;

    @Transactional
    public FileGroupDto save(ReceiptDto receiptDto, MultipartFile file, String userSub) {
        FileType fileType = FileType.invoke(file.getContentType());
        ReceiptQueryEntity receiptQueryEntity = new ReceiptQueryEntity(receiptDto.getId(), null);
        FileGroupEntity fileGroupEntity = new FileGroupEntity(fileType, receiptQueryEntity, true);
        FileEntity fileEntity = new FileEntity(null, null, fileGroupEntity, null, 0);
        String pathInString = createPath(userSub, fileEntity, fileGroupEntity, fileType.getExtension(), receiptDto);
        fileEntity.setPath(pathInString);
        fileGroupEntity.addFile(fileEntity);
        saveFile(pathInString, file);
        return toDto(fileGroupEntity);
    }

    private FileGroupDto toDto(FileGroupEntity fileGroupEntity) {
        return new FileGroupDto(fileGroupEntity.getFiles().stream().map(this::toDto).collect(Collectors.toSet()));
    }

    private FileDto toDto(FileEntity fileEntity) {
        return new FileDto(fileEntity.getId(), fileEntity.getPath(), fileEntity.getRawData(), fileEntity.getPartNumber());
    }


    private String createPath(
        String currentUserSub,
        FileEntity file,
        FileGroupEntity fileEntity,
        String fileExtension,
        ReceiptDto receipt
    ) {
        return String.format(FILE_PATH_TEMPLATE,
            currentUserSub, receipt.getId(), fileEntity.getId(), file.getId(), fileExtension);
    }

    FileSystemResource getFile(String receiptFileId) {
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

    private void saveFile(String path, MultipartFile file) {
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

}