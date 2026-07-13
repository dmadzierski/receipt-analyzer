package pl.madzierski.daniel.app.file_group;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.app.common.model.BaseEntity;
import pl.madzierski.daniel.app.file_group.file.FileEntity;
import pl.madzierski.daniel.app.receipt.ReceiptEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "receipt_file_group")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FileGroupEntity extends BaseEntity {

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "fileGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<FileEntity> files = new HashSet<>();
    @Enumerated(EnumType.STRING)
    private FileType fileType;
    @ManyToOne
    @JoinColumn(name = "receipt_id")
    private ReceiptEntity receipt;
    private Boolean isOriginal;

    public void addFile(FileEntity fileEntity) {
        this.files.add(fileEntity);
    }

    public Set<FileEntity> getFiles() {
        return files.stream().collect(Collectors.toUnmodifiableSet());
    }
}