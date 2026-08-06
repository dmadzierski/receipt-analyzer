package pl.madzierski.daniel.file_group;

import pl.madzierski.daniel.file_group.model.FileDto;

import java.util.Optional;

public interface FileQueryRepository {
    Optional<FileDto> findOriginalPdf(String receiptId);
}
