package pl.madzierski.daniel.file_group;

import pl.madzierski.daniel.file_group.model.FileGroupDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileGroupQueryRepository {
    List<FileGroupDto> findAllByReceiptId(UUID receiptId);

    Optional<FileGroupDto> findFileGroupWithFiles(UUID fileGroupId);

    boolean isUsedByRevision(UUID fileGroupId);
}
