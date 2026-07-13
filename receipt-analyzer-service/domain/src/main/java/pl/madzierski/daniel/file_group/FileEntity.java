package pl.madzierski.daniel.file_group;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "receipt_file")
@NoArgsConstructor
@Getter
@Setter
@EntityListeners({AuditingEntityListener.class})
class FileEntity {

    @Id
    @UuidGenerator
    private String id;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_group_id")
    private FileGroupEntity fileGroup;

    private String path;

    @Column(name = "raw_data", columnDefinition = "TEXT")
    private String rawData;

    private Integer partNumber;

    public FileEntity(String id, String path, FileGroupEntity fileGroup, String rawData, Integer partNumber) {
        this.id = id;
        this.path = path;
        this.fileGroup = fileGroup;
        this.rawData = rawData;
        this.partNumber = partNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        FileEntity that = (FileEntity) o;
        return Objects.equals(path, that.path) && Objects.equals(rawData, that.rawData) && Objects.equals(partNumber, that.partNumber);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(path);
        result = 31 * result + Objects.hashCode(rawData);
        result = 31 * result + Objects.hashCode(partNumber);
        return result;
    }

}