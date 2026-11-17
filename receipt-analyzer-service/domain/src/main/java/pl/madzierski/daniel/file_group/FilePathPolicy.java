package pl.madzierski.daniel.file_group;

@FunctionalInterface
interface FilePathPolicy {
    String pathOf(FileGroup fileGroup, File file);
}
