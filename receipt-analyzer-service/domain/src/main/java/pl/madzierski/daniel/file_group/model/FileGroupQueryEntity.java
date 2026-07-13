package pl.madzierski.daniel.file_group.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "receipt_file_group")
@NoArgsConstructor
@AllArgsConstructor
public class FileGroupQueryEntity {
    @Id
    @UuidGenerator
    private String id;
}
