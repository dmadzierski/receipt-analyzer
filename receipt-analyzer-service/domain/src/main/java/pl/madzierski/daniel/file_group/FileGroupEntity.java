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
import pl.madzierski.daniel.receipt.model.ReceiptQueryEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "receipt_file_group")
@NoArgsConstructor
@Getter
@Setter
@EntityListeners({AuditingEntityListener.class})
class FileGroupEntity {

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "fileGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<FileEntity> files = new HashSet<>();
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
    private ReceiptQueryEntity receipt;
    private Boolean isOriginal;

    public FileGroupEntity(FileType fileType, ReceiptQueryEntity receipt, Boolean isOriginal) {
        this.fileType = fileType;
        this.receipt = receipt;
        this.isOriginal = isOriginal;
    }

    void addFile(FileEntity file) {
        this.files.add(file);
    }

    Set<FileEntity> getFiles() {
        return Collections.unmodifiableSet(this.files);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        FileGroupEntity that = (FileGroupEntity) o;
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