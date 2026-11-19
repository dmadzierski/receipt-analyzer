package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.file_group.model.FileContentDto;
import pl.madzierski.daniel.receipt.model.ReceiptQuery;
import pl.madzierski.daniel.user.model.AuthenticatedUser;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
public class FileFacade {

    private final FileRepository fileRepository;
    private final FileGroupRepository fileGroupRepository;
    private final FileTypeDetector fileTypeDetector;
    private final FileStorage fileStorage;
    private final String uploadDir;

    @Transactional
    public UUID save(AuthenticatedUser user, UUID receiptId, List<byte[]> files) {
        if (!fileGroupRepository.isReceiptAccessible(new ReceiptQuery(receiptId), user.toUserQuery()))
            throw new AppRuntimeException(AppRuntimeExceptionMessages.RECEIPT_NOT_FOUND);
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
        FileGroup savedFileGroup = fileGroupRepository.save(
            FileGroup.create(fileType, new ReceiptQuery(receiptId), user.toUserQuery(), files.size()));
        savedFileGroup.assignPaths(this::createPath);
        FileGroup fileGroupWithPaths = fileGroupRepository.save(savedFileGroup);
        fileGroupWithPaths.getFiles().forEach(file -> fileStorage.save(file.getPath(), files.get(file.getPartNumber())));
        return fileGroupWithPaths.getId();
    }

    private String createPath(FileGroup fileGroup, File file) {
        return uploadDir + java.io.File.separator + "user" + java.io.File.separator +
               fileGroup.getOwner().id() + java.io.File.separator + "receipts" + java.io.File.separator +
               fileGroup.getReceipt().id() + java.io.File.separator + "file_group" + java.io.File.separator +
               fileGroup.getId() + java.io.File.separator + "file" + java.io.File.separator +
               file.getId() + "." + fileGroup.getFileType().getExtension();
    }

    FileContentDto getFile(AuthenticatedUser user, UUID receiptFileId) {
        File file = fileRepository.findAccessible(receiptFileId, user.toUserQuery())
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.FILE_RECEIPT_NOT_FOUND));
        if (file.getPath() == null)
            throw new AppRuntimeException(AppRuntimeExceptionMessages.FILE_RECEIPT_PATH_NOT_FOUND);
        return new FileContentDto(Path.of(file.getPath()).getFileName().toString(), readFile(file.getPath()));
    }

    public List<byte[]> getFiles(UUID receiptId, UUID fileGroupId) {
        return fileGroupRepository.findByIdAndReceipt(fileGroupId, new ReceiptQuery(receiptId))
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.FILE_GROUP_NOT_FOUND))
            .getFiles()
            .stream()
            .sorted(Comparator.comparing(File::getPartNumber))
            .map(file -> readFile(file.getPath()))
            .toList();
    }

    private byte[] readFile(String path) {
        return fileStorage.read(path)
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.FILE_RECEIPT_FILE_NOT_FOUND));
    }

    @Transactional
    public void deleteFileGroupId(AuthenticatedUser user, UUID fileGroupId) {
        FileGroup fileGroup = fileGroupRepository.findById(fileGroupId, user.toUserQuery())
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.FILE_GROUP_NOT_FOUND));
        if (fileGroupRepository.isUsedByRevision(fileGroupId))
            throw new AppRuntimeException(AppRuntimeExceptionMessages.FILE_GROUP_USED_BY_REVISION);
        fileGroupRepository.deleteById(fileGroupId);
        fileGroup.getFiles().stream()
            .map(File::getPath)
            .filter(Objects::nonNull)
            .forEach(fileStorage::delete);
    }

    @Transactional
    public void deleteFileGroupsByReceiptId(UUID receiptId) {
        List<String> paths = fileGroupRepository.findAllByReceipt(new ReceiptQuery(receiptId)).stream()
            .flatMap(fileGroup -> fileGroup.getFiles().stream())
            .map(File::getPath)
            .filter(Objects::nonNull)
            .toList();
        fileGroupRepository.deleteAllByReceiptId(receiptId);
        paths.forEach(fileStorage::delete);
    }
}