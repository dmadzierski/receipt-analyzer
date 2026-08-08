package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.annotation.Transactional;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.file_group.model.FileDto;
import pl.madzierski.daniel.file_group.model.FileGroupDto;
import pl.madzierski.daniel.receipt.model.ReceiptDto;
import pl.madzierski.daniel.receipt.model.ReceiptQuery;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.stream.Collectors;

@AllArgsConstructor
public class FileFacade {

    public static final String FILE_PATH_TEMPLATE = "/app/uploads/user/%s/receipts/%s/file_group/%s/file/%s.%s";
    private final FileRepository fileRepository;

    @Transactional
    public FileGroupDto save(ReceiptDto receiptDto, InputStream file, String contentType, String userSub) {
        FileType fileType = FileType.invoke(contentType);
        ReceiptQuery receiptQueryEntity = new ReceiptQuery(receiptDto.getId(), null);
        FileGroup fileGroup = new FileGroup(fileType, receiptQueryEntity, true);
        File fileEntity = new File(null, null, fileGroup, null, 0);
        String pathInString = createPath(userSub, fileEntity, fileGroup, fileType.getExtension(), receiptDto);
        fileEntity.setPath(pathInString);
        fileGroup.addFile(fileEntity);
        saveFile(pathInString, file);
        return toDto(fileGroup);
    }

    private FileGroupDto toDto(FileGroup fileGroup) {
        return new FileGroupDto(fileGroup.getFiles().stream().map(this::toDto).collect(Collectors.toSet()));
    }

    private FileDto toDto(File file) {
        return new FileDto(file.getId(), file.getPath(), file.getRawData(), file.getPartNumber());
    }


    private String createPath(
        String currentUserSub,
        File file,
        FileGroup fileEntity,
        String fileExtension,
        ReceiptDto receipt
    ) {
        return String.format(FILE_PATH_TEMPLATE,
            currentUserSub, receipt.getId(), fileEntity.getId(), file.getId(), fileExtension);
    }

    FileSystemResource getFile(String receiptFileId) {
        File fileEntity = fileRepository.findById(receiptFileId)
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.FILE_RECEIPT_NOT_FOUND));
        String path = fileEntity.getPath();
        if (path == null) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.FILE_RECEIPT_PATH_NOT_FOUND);
        }

        java.io.File file = new java.io.File(path);

        if (!file.exists()) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.FILE_RECEIPT_FILE_NOT_FOUND);
        }

        return new FileSystemResource(file);
    }

    private void saveFile(String path, InputStream file) {
        try {
            java.io.File destFile = new java.io.File(path);
            if (destFile.getParentFile() != null) {
                destFile.getParentFile().mkdirs();
            }
            try (OutputStream outputStream = Files.newOutputStream(destFile.toPath())) {
                file.transferTo(outputStream);
            }
        } catch (IOException e) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.UNHANDLED_FILE_TYPE);
        }
    }

}