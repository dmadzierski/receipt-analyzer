package pl.madzierski.daniel.file_group;

public interface FileStorage {
    void save(String path, byte[] content);
}
