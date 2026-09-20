package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.file_group.model.CreateFileGroupRequest;
import pl.madzierski.daniel.file_group.model.FileDto;
import pl.madzierski.daniel.file_group.model.FileGroupDto;
import pl.madzierski.daniel.receipt.model.ReceiptQuery;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class FileFacade {

    private static final String FILE_PATH_TEMPLATE = "/app/uploads/user/%s/receipts/%s/file_group/%s/file/%s.%s";
    private final FileRepository fileRepository;
    private final FileGroupQueryRepository fileGroupQueryRepository;
    private final FileGroupRepository fileGroupRepository;

    public FileGroupDto save(String receiptId, byte[] binaryFile, String contentType, String userSub) {
        FileType fileType = FileType.invoke(contentType);
        ReceiptQuery receiptQueryEntity = new ReceiptQuery(receiptId);
        FileGroup fileGroup = new FileGroup(fileType, receiptQueryEntity, true);
        fileGroup = fileGroupRepository.save(fileGroup);
        File file = new File(fileGroup, 0);
        file.setFileGroup(fileGroup);
        file = fileRepository.save(file);
        String pathInString = createPath(userSub, file.getId(), fileGroup.getId(),
            fileType.getExtension(),
            receiptId);
        file.setPath(pathInString);
        fileRepository.save(file);
        saveFile(pathInString, binaryFile);
        return toDto(fileGroup);
    }

    private FileGroupDto toDto(FileGroup fileGroup) {
        return new FileGroupDto(fileGroup.getId(),
            fileGroup.getFileType(),
            fileGroup.getIsOriginal(),
            fileGroup.getFiles()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toSet()));
    }

    private FileDto toDto(File file) {
        return new FileDto(file.getId(), file.getPath(), file.getRawData(), file.getPartNumber());
    }

    public String createPath(
        String currentUserSub,
        String fileId,
        String fileGroupId,
        String fileExtension,
        String receiptId
    ) {
        return String.format(FILE_PATH_TEMPLATE,
            currentUserSub, receiptId, fileGroupId, fileId, fileExtension);
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

    public void uploadFile(String userSub, String receiptId, List<MultipartFile> files, CreateFileGroupRequest createFileGroupRequest) {
        if (files == null || files.isEmpty()) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.FILE_NOT_FOUND);
        }
        FileType fileType = createFileGroupRequest.fileType();
        ReceiptQuery receiptQueryEntity = new ReceiptQuery(receiptId);
        FileGroup fileGroup = new FileGroup(fileType, receiptQueryEntity, createFileGroupRequest.isOriginal());
        for (int partNumber = 0; partNumber < files.size(); partNumber++) {
            MultipartFile multipartFile = files.get(partNumber);
            File fileEntity = new File(fileGroup, partNumber);
            String pathInString = createPath(userSub, fileEntity.getId(), fileGroup.getId(), fileType.getExtension(),
                receiptId);
            fileEntity.setPath(pathInString);
            fileGroup.addFile(fileEntity);
            try {
                saveFile(pathInString, multipartFile.getInputStream()
                    .readAllBytes());
            } catch (IOException e) {
                throw new AppRuntimeException(AppRuntimeExceptionMessages.ERROR_DURING_SAVING_FILE);
            }
        }
        fileGroupRepository.save(fileGroup);
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
}