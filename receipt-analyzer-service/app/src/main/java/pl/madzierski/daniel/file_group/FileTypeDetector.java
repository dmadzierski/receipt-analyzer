package pl.madzierski.daniel.file_group;

public interface FileTypeDetector {

    FileType detectFileType(byte[] file);
}
