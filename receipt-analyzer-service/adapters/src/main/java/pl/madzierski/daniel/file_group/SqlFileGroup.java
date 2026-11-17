package pl.madzierski.daniel.file_group;


import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.common.entity.BaseEntity;
import pl.madzierski.daniel.receipt.SqlReceiptQuery;
import pl.madzierski.daniel.user.model.UserQuery;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
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
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;
    @Getter(AccessLevel.NONE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "file_group_id", nullable = false)
    private final Set<SqlFile> files = new HashSet<>();

    public static SqlFileGroup fromFileGroup(FileGroup fileGroup) {
        SqlFileGroup sqlFileGroup = new SqlFileGroup();
        sqlFileGroup.setId(fileGroup.getId());
        sqlFileGroup.setCreatedDate(fileGroup.getCreatedDate());
        sqlFileGroup.setModifiedDate(fileGroup.getModifiedDate());
        sqlFileGroup.setFileType(fileGroup.getFileType());
        sqlFileGroup.setOwnerId(fileGroup.getOwner() != null ? fileGroup.getOwner().id() : null);
        sqlFileGroup.setReceipt(fileGroup.getReceipt() != null ? new SqlReceiptQuery(fileGroup.getReceipt().id()) :
            null);
        sqlFileGroup.addFiles(fileGroup.getFiles().stream().map(SqlFile::fromFile).collect(Collectors.toSet()));
        return sqlFileGroup;
    }

    private void addFiles(Set<SqlFile> collect) {
        this.files.addAll(collect);
    }

    Set<SqlFile> getFiles() {
        return Collections.unmodifiableSet(files);
    }

    public FileGroup toFileGroup() {
        return FileGroup.restore(id, createdDate, modifiedDate, fileType, receipt.toReceipt(),
            ownerId != null ? new UserQuery(ownerId) : null, files.stream().map(SqlFile::toFile).toList());
    }
}
