package pl.madzierski.daniel.file_group;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pl.madzierski.daniel.common.entity.BaseEntity;
import pl.madzierski.daniel.file_group.model.FileGroupQuery;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "receipt_file_group")
@NoArgsConstructor
public class SqlFileGroupQuery extends BaseEntity {

    public SqlFileGroupQuery(String id) {
        this.id = id;
    }

    public static SqlFileGroupQuery fromFileGroupQuery(FileGroupQuery sourceFileGroup) {
        return new SqlFileGroupQuery(sourceFileGroup.getId());
    }

    public FileGroupQuery toFileGroupQuery() {
        return new FileGroupQuery(id);
    }
}
