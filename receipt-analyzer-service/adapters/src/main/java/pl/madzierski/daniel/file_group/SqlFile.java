package pl.madzierski.daniel.file_group;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.madzierski.daniel.common.entity.BaseEntity;


@EqualsAndHashCode(callSuper = true, exclude = "fileGroup")
@Entity
@Table(name = "receipt_file")
@Setter
@Getter
@NoArgsConstructor
class SqlFile extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_group_id", insertable = false, updatable = false)
    private SqlFileGroup fileGroup;
    private String path;
    @Column(name = "raw_data", columnDefinition = "TEXT")
    private String rawData;
    private Integer partNumber;

    static SqlFile fromFile(File file) {
        SqlFile sqlFile = new SqlFile();
        sqlFile.id = file.getId();
        sqlFile.path = file.getPath();
        sqlFile.rawData = file.getRawData();
        sqlFile.partNumber = file.getPartNumber();
        return sqlFile;
    }

    File toFile() {
        File file = new File();
        file.setId(this.id);
        file.setPath(this.path);
        file.setRawData(this.rawData);
        file.setPartNumber(this.partNumber);
        return file;
    }

}
