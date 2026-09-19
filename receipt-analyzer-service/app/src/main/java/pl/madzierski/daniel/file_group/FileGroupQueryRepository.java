package pl.madzierski.daniel.file_group;

import pl.madzierski.daniel.file_group.model.FileGroupDto;

import java.util.List;

public interface FileGroupQueryRepository {
    List<FileGroupDto> findAllByReceiptId(String receiptId);
}
