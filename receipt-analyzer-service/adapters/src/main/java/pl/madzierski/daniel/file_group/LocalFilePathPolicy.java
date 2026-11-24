package pl.madzierski.daniel.file_group;

import org.springframework.stereotype.Component;

@Component
class LocalFilePathPolicy implements FilePathPolicy {

    @Override
    public String pathOf(FileGroup fileGroup, File file) {
        return String.join("/", "user", fileGroup.getOwner().id().toString(), "receipts", fileGroup.getReceiptId().toString(),
            "file_group", fileGroup.getId().toString(), "file", file.getId() + "." + fileGroup.getFileType().getExtension());
    }
}
