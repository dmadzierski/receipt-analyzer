package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.file_group.model.FileGroupDto;
import pl.madzierski.daniel.receipt.model.ReceiptQuery;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@AllArgsConstructor
public class FileFacade {

    private final FileRepository fileRepository;
    private final FileGroupQueryRepository fileGroupQueryRepository;
    private final FileGroupRepository fileGroupRepository;
    private final FileGroupFactory fileGroupFactory;
    private final FileTypeDetector fileTypeDetector;
    private final String uploadDir;

    public FileGroupDto save(String receiptId, List<byte[]> files, String userSub) {
        if (files == null || files.isEmpty()) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.FILE_NOT_FOUND);
        }
        FileType fileType = null;
        for (byte[] content : files) {
            if (fileType == null)
                fileType = fileTypeDetector.detectFileType(content);
            else if (fileTypeDetector.detectFileType(content) != fileType) {
                throw new AppRuntimeException(AppRuntimeExceptionMessages.FILE_TYPE_MISMATCH);
            }
        }
        if (fileType == null) throw new AppRuntimeException(AppRuntimeExceptionMessages.FILE_TYPE_NOT_DETECTED);
        FileGroup fileGroup = fileGroupRepository.save(new FileGroup(fileType, new ReceiptQuery(receiptId), true));
        for (int partNumber = 0; partNumber < files.size(); partNumber++) {
            File file = new File(fileGroup, partNumber);
            file = fileRepository.save(file);
            String path = createPath(userSub, file.getId(), fileGroup.getId(), fileType.getExtension(), receiptId);
            file.setPath(path);
            fileRepository.save(file);
            saveFile(path, files.get(partNumber));
        }
        return fileGroupFactory.toDto(fileGroup);
    }

    String createPath(String currentUserSub, String fileId, String fileGroupId, String fileExtension, String receiptId) {
        return uploadDir + java.io.File.separator + "user" + java.io.File.separator +
               currentUserSub + java.io.File.separator + "receipts" + java.io.File.separator +
               receiptId + java.io.File.separator + "file_group" + java.io.File.separator +
               fileGroupId + java.io.File.separator + "file" + java.io.File.separator +
               fileId + "." + fileExtension;
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

    private void saveFile(String path, byte[] file) {
        try {
            java.io.File destFile = new java.io.File(path);
            if (destFile.getParentFile() != null) {
                destFile.getParentFile()
                    .mkdirs();
            }
            try (OutputStream outputStream = Files.newOutputStream(destFile.toPath())) {
                outputStream.write(file);
            }
        } catch (IOException e) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.ERROR_DURING_SAVING_FILE);
        }
    }

    public List<byte[]> getFiles(String fileGroupId) {
        return fileGroupQueryRepository.findFileGroupWithFiles(fileGroupId)
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.FILE_GROUP_NOT_FOUND))
            .files()
            .stream()
            .map(file -> {
                try {
                    return Files.readAllBytes(Paths.get(file.getPath()));
                } catch (IOException e) {
                    throw new AppRuntimeException(AppRuntimeExceptionMessages.FILE_RECEIPT_FILE_NOT_FOUND);
                }
            })
            .toList();
    }

    public void deleteFileGroupId(String fileGroupId, String userSub) {
        if (fileGroupQueryRepository.isUsedByRevision(fileGroupId))
            throw new AppRuntimeException(AppRuntimeExceptionMessages.FILE_GROUP_USED_BY_REVISION);
        fileGroupRepository.deleteById(fileGroupId);
    }

    public void deleteFileGroupsByReceiptId(String receiptId) {
        fileGroupRepository.deleteAllByReceiptId(receiptId);
    }
}