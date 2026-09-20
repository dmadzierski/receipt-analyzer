package pl.madzierski.daniel.file_group;

public interface FileGroupRepository {
    FileGroup save(FileGroup fileGroup);
    void flush();
}
