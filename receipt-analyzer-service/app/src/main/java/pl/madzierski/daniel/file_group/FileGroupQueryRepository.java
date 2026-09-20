package pl.madzierski.daniel.file_group;

import pl.madzierski.daniel.file_group.model.FileGroupDto;

import java.util.List;
import java.util.Optional;

public interface FileGroupQueryRepository {
    List<FileGroupDto> findAllByReceiptId(String receiptId);

    Optional<FileGroupDto> findFileGroupWithFiles(String fileGroupId);
}
