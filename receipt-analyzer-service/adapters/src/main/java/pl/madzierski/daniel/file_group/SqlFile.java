package pl.madzierski.daniel.file_group;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.madzierski.daniel.common.entity.BaseEntity;


@EqualsAndHashCode(callSuper = true, exclude = "fileGroup")
@Entity
@Table(name = "receipt_file")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class SqlFile extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_group_id", insertable = false, updatable = false)
    private SqlFileGroup fileGroup;
    private String path;
    private Integer partNumber;

    static SqlFile fromFile(File file) {
        SqlFile sqlFile = new SqlFile();
        sqlFile.id = file.getId();
        sqlFile.createdDate = file.getCreatedDate();
        sqlFile.modifiedDate = file.getModifiedDate();
        sqlFile.path = file.getPath();
        sqlFile.partNumber = file.getPartNumber();
        return sqlFile;
    }

    File toFile() {
        return File.restore(id, createdDate, modifiedDate, path, partNumber);
    }

}
