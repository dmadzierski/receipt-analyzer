package pl.madzierski.daniel.file_group;

import pl.madzierski.daniel.receipt.model.ReceiptQuery;
import pl.madzierski.daniel.user.model.UserQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileGroupRepository {
    FileGroup save(FileGroup fileGroup);

    Optional<FileGroup> findById(UUID fileGroupId, UserQuery user);

    Optional<FileGroup> findByIdAndReceipt(UUID fileGroupId, ReceiptQuery receipt);

    List<FileGroup> findAllByReceipt(ReceiptQuery receipt);

    boolean isReceiptAccessible(ReceiptQuery receipt, UserQuery user);

    boolean isUsedByRevision(UUID fileGroupId);

    void deleteById(UUID fileGroupId);

    void deleteAllByReceiptId(UUID receiptId);
}
