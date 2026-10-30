package pl.madzierski.daniel.file_group;

import java.util.Optional;
import java.util.UUID;

interface FileRepository {
    Optional<File> findById(UUID fileId);

    File save(File fileEntity);
}