package pl.madzierski.daniel.file_group;

import pl.madzierski.daniel.user.model.UserQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface FileGroupRepository {
    FileGroup save(FileGroup fileGroup);

    Optional<FileGroup> findById(UUID fileGroupId, UserQuery user);

    Optional<FileGroup> findByIdAndReceiptId(UUID fileGroupId, UUID receiptId);

    List<FileGroup> findAllByReceiptId(UUID receiptId);

    boolean isReceiptAccessible(UUID receiptId, UserQuery user);

    boolean isUsedByRevision(UUID fileGroupId);

    void deleteById(UUID fileGroupId);

    void deleteAllByReceiptId(UUID receiptId);
}
