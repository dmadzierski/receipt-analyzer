package pl.madzierski.daniel.file_group;

import pl.madzierski.daniel.file_group.model.FileDto;

import java.util.Optional;
import java.util.UUID;

public interface FileQueryRepository {
    Optional<FileDto> findOriginalPdf(UUID receiptId);
}
