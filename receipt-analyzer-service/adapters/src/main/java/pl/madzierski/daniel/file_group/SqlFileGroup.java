package pl.madzierski.daniel.file_group;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.madzierski.daniel.receipt.SqlReceiptQuery;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "receipt_file_group")
@NoArgsConstructor
@Setter
@EntityListeners({AuditingEntityListener.class})
class SqlFileGroup {
    public static SqlFileGroup fromFileGroup(FileGroup fileGroup) {
        SqlFileGroup sqlFileGroup = new SqlFileGroup();
        sqlFileGroup.setId(fileGroup.getId());
        sqlFileGroup.setCreatedDate(fileGroup.getCreatedDate());
        sqlFileGroup.setModifiedDate(fileGroup.getModifiedDate());
        sqlFileGroup.setFileType(fileGroup.getFileType());
        sqlFileGroup.setIsOriginal(fileGroup.getIsOriginal());
        sqlFileGroup.setReceipt(fileGroup.getReceipt() != null ? new SqlReceiptQuery(fileGroup.getReceipt().getId()) :
            null);
        sqlFileGroup.addFiles(fileGroup.getFiles().stream().map(SqlFile::fromFile).collect(Collectors.toSet()));
        sqlFileGroup.files.forEach(sqlFile -> sqlFile.setFileGroup(sqlFileGroup));
        return sqlFileGroup;
    }

    @Id
    @UuidGenerator
    private String id;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    @LastModifiedDate
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;
    @Enumerated(EnumType.STRING)
    private FileType fileType;
    @ManyToOne
    @JoinColumn(name = "receipt_id")
    private SqlReceiptQuery receipt;
    private Boolean isOriginal;
    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "fileGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<SqlFile> files = new HashSet<>();

    private void addFiles(Set<SqlFile> collect) {
        this.files.addAll(collect);
    }

    public FileGroup toFileGroupDto() {
        FileGroup fileGroup = new FileGroup();
        fileGroup.setFileType(this.fileType);
        fileGroup.setReceipt(this.receipt.toReceipt());
        fileGroup.setIsOriginal(this.isOriginal);
        fileGroup.setCreatedDate(this.createdDate);
        fileGroup.setModifiedDate(this.modifiedDate);
        fileGroup.setId(id);
        fileGroup.addFiles(this.files.stream().map(SqlFile::toFile).collect(Collectors.toSet()));
        return fileGroup;
    }
}
