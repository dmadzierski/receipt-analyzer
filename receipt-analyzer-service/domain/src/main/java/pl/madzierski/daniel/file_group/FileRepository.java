package pl.madzierski.daniel.file_group;

import java.util.Optional;

interface FileRepository {
    Optional<File> findById(String fileId);
    FileGroup save(FileGroup fileGroup);
}