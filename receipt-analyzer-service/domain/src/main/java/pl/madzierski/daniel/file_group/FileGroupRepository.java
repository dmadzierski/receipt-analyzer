package pl.madzierski.daniel.file_group;

import java.util.UUID;

public interface FileGroupRepository {

    FileGroup save(FileGroup fileGroup);

    void flush();

    void deleteById(UUID fileGroupId);

    void deleteAllByReceiptId(UUID receiptId);
}
