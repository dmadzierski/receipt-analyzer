package pl.madzierski.daniel.file_group;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.madzierski.daniel.receipt.model.ReceiptQuery;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(exclude = {"receipt", "files"})
class FileGroup {

    private UUID id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private FileType fileType;
    private ReceiptQuery receipt;
    private Boolean isOriginal;
    private final Set<File> files = new HashSet<>();

    public FileGroup(FileType fileType, ReceiptQuery receipt, Boolean isOriginal) {
        this.fileType = fileType;
        this.receipt = receipt;
        this.isOriginal = isOriginal;
    }

    public void addFiles(Set<File> collect) {
        this.files.addAll(collect);
    }

    Set<File> getFiles() {
        return Collections.unmodifiableSet(this.files);
    }
}