package pl.madzierski.daniel.file_group;


import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.common.entity.BaseEntity;
import pl.madzierski.daniel.receipt.SqlReceiptQuery;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "receipt_file_group")
@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(callSuper = true, exclude = {"receipt", "files"})
class SqlFileGroup extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private FileType fileType;
    @ManyToOne
    @JoinColumn(name = "receipt_id")
    private SqlReceiptQuery receipt;
    private Boolean isOriginal;
    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "fileGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<SqlFile> files = new HashSet<>();

    public static SqlFileGroup fromFileGroup(FileGroup fileGroup) {
        SqlFileGroup sqlFileGroup = new SqlFileGroup();
        sqlFileGroup.setId(fileGroup.getId());
        sqlFileGroup.setCreatedDate(fileGroup.getCreatedDate());
        sqlFileGroup.setModifiedDate(fileGroup.getModifiedDate());
        sqlFileGroup.setFileType(fileGroup.getFileType());
        sqlFileGroup.setIsOriginal(fileGroup.getIsOriginal());
        sqlFileGroup.setReceipt(fileGroup.getReceipt() != null ? new SqlReceiptQuery(fileGroup.getReceipt().getId()) :
            null);
        sqlFileGroup.addFiles(fileGroup.getFiles().stream().map((File file) -> SqlFile.fromFile(file, sqlFileGroup)).collect(Collectors.toSet()));
        sqlFileGroup.files.forEach(sqlFile -> sqlFile.setFileGroup(sqlFileGroup));
        return sqlFileGroup;
    }

    private void addFiles(Set<SqlFile> collect) {
        this.files.addAll(collect);
    }

    Set<SqlFile> getFiles() {
        return Collections.unmodifiableSet(files);
    }

    public FileGroup toFileGroup() {
        FileGroup fileGroup = new FileGroup();
        fileGroup.setFileType(this.fileType);
        fileGroup.setReceipt(this.receipt.toReceipt());
        fileGroup.setIsOriginal(this.isOriginal);
        fileGroup.setCreatedDate(this.createdDate);
        fileGroup.setModifiedDate(this.modifiedDate);
        fileGroup.setId(id);
        fileGroup.addFiles(this.files.stream().map((SqlFile file) -> file.toFile(fileGroup)).collect(Collectors.toSet()));
        return fileGroup;
    }
}
