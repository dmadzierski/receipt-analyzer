package pl.madzierski.daniel.file_group;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import pl.madzierski.daniel.file_group.model.FileGroupQuery;

@Entity
@Table(name = "receipt_file_group")
@NoArgsConstructor
@AllArgsConstructor
public class SqlFileGroupQuery {
    @Id
    @UuidGenerator
    private String id;

    public static SqlFileGroupQuery fromFileGroupQuery(FileGroupQuery sourceFileGroup) {
        return new SqlFileGroupQuery(sourceFileGroup.getId());
    }
}
