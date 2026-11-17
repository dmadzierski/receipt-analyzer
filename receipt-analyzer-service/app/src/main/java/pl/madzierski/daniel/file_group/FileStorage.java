package pl.madzierski.daniel.file_group;

import java.util.Optional;

public interface FileStorage {
    void save(String path, byte[] content);

    Optional<byte[]> read(String path);

    void delete(String path);
}
