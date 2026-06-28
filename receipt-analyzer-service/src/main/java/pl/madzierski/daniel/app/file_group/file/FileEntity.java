package pl.madzierski.daniel.app.file_group.file;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.madzierski.daniel.app.common.model.BaseEntity;
import pl.madzierski.daniel.app.file_group.FileGroupEntity;

@Entity
@Table(name = "receipt_file")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FileEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_group_id")
    private FileGroupEntity fileGroup;

    private String path;

    @Column(name = "raw_data", columnDefinition = "TEXT")
    private String rawData;

    private Integer partNumber;
}