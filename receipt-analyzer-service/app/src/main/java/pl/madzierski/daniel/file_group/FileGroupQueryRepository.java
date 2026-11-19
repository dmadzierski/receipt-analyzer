package pl.madzierski.daniel.file_group;

import pl.madzierski.daniel.file_group.model.FileGroupDto;

import java.util.List;
import java.util.UUID;

public interface FileGroupQueryRepository {
    List<FileGroupDto> findAllByReceiptId(UUID receiptId);
}
