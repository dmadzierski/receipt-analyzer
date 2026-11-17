package pl.madzierski.daniel.file_group;

import pl.madzierski.daniel.user.model.UserQuery;

import java.util.Optional;
import java.util.UUID;

public interface FileGroupRepository {

    FileGroup save(FileGroup fileGroup);

    Optional<FileGroup> findById(UUID fileGroupId, UserQuery user);

    void deleteById(UUID fileGroupId);

    void deleteAllByReceiptId(UUID receiptId);
}
