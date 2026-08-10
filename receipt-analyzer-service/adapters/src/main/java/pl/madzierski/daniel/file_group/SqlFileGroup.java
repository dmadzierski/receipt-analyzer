package pl.madzierski.daniel.file_group;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.madzierski.daniel.receipt.SqlReceiptQuery;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "receipt_file_group")
@NoArgsConstructor
@EntityListeners({AuditingEntityListener.class})
class SqlFileGroup {

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

    static SqlFileGroup fromFileGroup(FileGroup fileGroup) {
        SqlFileGroup sqlFileGroup = new SqlFileGroup();
        sqlFileGroup.fileType = fileGroup.getFileType();
        sqlFileGroup.receipt = SqlReceiptQuery.fromReceipt(fileGroup.getReceipt());
        sqlFileGroup.isOriginal = fileGroup.getIsOriginal();
        return sqlFileGroup;
    }

    public FileGroup toFileGroup() {
        FileGroup fileGroup = new FileGroup();
        fileGroup.setFileType(this.fileType);
        fileGroup.setReceipt(this.receipt.toReceipt());
        fileGroup.setIsOriginal(this.isOriginal);
        return fileGroup;
    }
}
