package pl.madzierski.daniel.file_group;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "receipt_file")
@EntityListeners({AuditingEntityListener.class})
@NoArgsConstructor
class SqlFile {

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
    private SqlFileGroup fileGroup;
    private String path;
    @Column(name = "raw_data", columnDefinition = "TEXT")
    private String rawData;
    private Integer partNumber;

    static SqlFile fromFile(File file) {
        SqlFile sqlFile = new SqlFile();
        sqlFile.id = file.getId();
        sqlFile.path = file.getPath();
        sqlFile.fileGroup = SqlFileGroup.fromFileGroup(file.getFileGroup());
        sqlFile.rawData = file.getRawData();
        sqlFile.partNumber = file.getPartNumber();
        return sqlFile;
    }

    File toFile() {
        File file = new File();
        file.setId(this.id);
        file.setPath(this.path);
        file.setFileGroup(this.fileGroup.toFileGroup());
        file.setRawData(this.rawData);
        file.setPartNumber(this.partNumber);
        return file;
    }
}
