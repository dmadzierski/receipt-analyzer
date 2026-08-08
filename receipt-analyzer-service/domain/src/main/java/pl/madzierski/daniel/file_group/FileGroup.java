package pl.madzierski.daniel.file_group;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.madzierski.daniel.receipt.model.ReceiptQuery;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
class FileGroup {

    private final Set<File> files = new HashSet<>();
    private String id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private FileType fileType;
    private ReceiptQuery receipt;
    private Boolean isOriginal;

    public FileGroup(FileType fileType, ReceiptQuery receipt, Boolean isOriginal) {
        this.fileType = fileType;
        this.receipt = receipt;
        this.isOriginal = isOriginal;
    }

    void addFile(File file) {
        this.files.add(file);
    }

    Set<File> getFiles() {
        return Collections.unmodifiableSet(this.files);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        FileGroup that = (FileGroup) o;
        return fileType == that.fileType && Objects.equals(isOriginal, that.isOriginal);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(fileType);
        result = 31 * result + Objects.hashCode(isOriginal);
        return result;
    }
}